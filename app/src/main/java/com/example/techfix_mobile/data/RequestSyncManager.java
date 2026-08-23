package com.example.techfix_mobile.data;

import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * One-shot pull sync: Firestore -> SQLite. No live listener (per team decision) —
 * call sync() on launch and from pull-to-refresh in MyRequestsActivity.
 */
public class RequestSyncManager {

    public interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    private final DatabaseHelper dbHelper;
    private final FirebaseFirestore firestore;

    public RequestSyncManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.firestore = FirebaseFirestore.getInstance();
    }

    /** Pulls this customer's repairRequests + their payments and writes them into SQLite. */
    public void sync(String customerUid, SyncCallback callback) {
        firestore.collection("repairRequests")
                .whereEqualTo("customerId", customerUid)
                .get()
                .addOnSuccessListener(requestSnapshots -> {
                    for (QueryDocumentSnapshot doc : requestSnapshots) {
                        RepairRequest r = doc.toObject(RepairRequest.class);
                        r.setRequestId(doc.getId());
                        r.setSyncStatus("synced");
                        dbHelper.upsertRequest(r);
                    }
                    syncPayments(customerUid, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSyncComplete(false);
                });
    }

    private void syncPayments(String customerUid, SyncCallback callback) {
        firestore.collection("payments")
                .whereEqualTo("customerId", customerUid)
                .get()
                .addOnSuccessListener(paymentSnapshots -> {
                    for (QueryDocumentSnapshot doc : paymentSnapshots) {
                        // Built manually instead of doc.toObject(Payment.class) because
                        // paidAt is written by the Cloud Function as a Firestore Timestamp
                        // (via FieldValue.serverTimestamp()), but our Payment model / SQLite
                        // schema store it as a plain long (millis) — the two don't auto-convert.
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

                        dbHelper.upsertPayment(p);
                    }
                    if (callback != null) callback.onSyncComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSyncComplete(false);
                });
    }
}
