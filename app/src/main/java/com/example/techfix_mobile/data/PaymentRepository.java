package com.example.techfix_mobile.data;

import com.example.techfix_mobile.model.Payment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface OnOrderCreated {
        void onCreated(String orderId);
        void onError(Exception e);
    }

    public interface OnPaymentChecked {
        void onResult(Payment payment); // null if not found
    }

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

    public void checkPaymentStatus(String orderId, OnPaymentChecked cb) {
        firestore.collection("payments").document(orderId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        cb.onResult(null);
                        return;
                    }
                    // Built manually instead of doc.toObject(Payment.class) — paidAt is a
                    // Firestore Timestamp once the webhook sets it, but our Payment model
                    // stores it as a plain long, and toObject() can't auto-convert that.
                    Payment p = new Payment();
                    p.setPaymentId(doc.getId());
                    p.setRequestId(doc.getString("requestId"));
                    p.setCustomerId(doc.getString("customerId"));
                    Double amount = doc.getDouble("amount");
                    p.setAmount(amount != null ? amount : 0.0);
                    String currency = doc.getString("currency");
                    p.setCurrency(currency != null ? currency : "LKR");
                    String status = doc.getString("status");
                    p.setStatus(status != null ? status : Payment.STATUS_PENDING);
                    p.setPayherePaymentId(doc.getString("payherePaymentId"));
                    p.setMethod(doc.getString("method"));

                    Timestamp paidAtTs = doc.getTimestamp("paidAt");
                    p.setPaidAt(paidAtTs != null ? paidAtTs.toDate().getTime() : 0L);

                    cb.onResult(p);
                })
                .addOnFailureListener(e -> cb.onResult(null));
    }

    public void hasExistingPayment(String requestId, java.util.function.Consumer<Boolean> cb) {
        firestore.collection("payments")
                .whereEqualTo("requestId", requestId)
                .whereIn("status", java.util.Arrays.asList(Payment.STATUS_PENDING, Payment.STATUS_COMPLETED))
                .get()
                .addOnSuccessListener(snap -> cb.accept(!snap.isEmpty()))
                .addOnFailureListener(e -> cb.accept(false));
    }
}