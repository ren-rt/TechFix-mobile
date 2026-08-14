package com.example.techfix_mobile.data;

import com.example.techfix_mobile.model.Payment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the Firestore side of payments. The Cloud Function (payhereNotify) is the
 * source of truth for whether a payment actually succeeded — this class creates the
 * pending doc before launching PayHere, and re-reads the doc afterwards to confirm.
 */
public class PaymentRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface OnOrderCreated {
        void onCreated(String orderId);
        void onError(Exception e);
    }

    public interface OnPaymentChecked {
        void onResult(Payment payment); // null if not found / still pending
    }

    /**
     * Step 1 of the payment flow: create a `payments` doc with status "pending" BEFORE
     * calling the PayHere SDK, so the webhook always has something to update.
     * Guards against double-tap by using a fresh UUID order_id each call — the caller
     * (PaymentActivity) should disable the Pay button immediately after tapping.
     */
    public void createPendingPayment(String requestId, String customerId, double amount, OnOrderCreated cb) {
        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 12);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("customerId", customerId);
        data.put("amount", amount);
        data.put("currency", "LKR");
        data.put("status", Payment.STATUS_PENDING);

        firestore.collection("payments").document(orderId)
                .set(data)
                .addOnSuccessListener(unused -> cb.onCreated(orderId))
                .addOnFailureListener(cb::onError);
    }

    /**
     * Step 2: after the PayHere SDK returns (onCompleted/onDismissed/onError), re-read
     * the doc rather than trusting the SDK callback alone — the Cloud Function may not
     * have written the final status yet, so callers should poll this a few times with
     * a short delay if the first check still shows "pending".
     */
    public void checkPaymentStatus(String orderId, OnPaymentChecked cb) {
        firestore.collection("payments").document(orderId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        cb.onResult(null);
                        return;
                    }
                    Payment p = doc.toObject(Payment.class);
                    if (p != null) p.setPaymentId(doc.getId());
                    cb.onResult(p);
                })
                .addOnFailureListener(e -> cb.onResult(null));
    }

    /** Prevents double payment: checks if a non-failed payment already exists for this request. */
    public void hasExistingPayment(String requestId, java.util.function.Consumer<Boolean> cb) {
        firestore.collection("payments")
                .whereEqualTo("requestId", requestId)
                .whereIn("status", java.util.Arrays.asList(Payment.STATUS_PENDING, Payment.STATUS_COMPLETED))
                .get()
                .addOnSuccessListener(snap -> cb.accept(!snap.isEmpty()))
                .addOnFailureListener(e -> cb.accept(false));
    }
}
