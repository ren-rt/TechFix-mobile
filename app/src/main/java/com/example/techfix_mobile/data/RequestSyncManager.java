package com.example.techfix_mobile.data;

import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/**
 * One-shot pull sync: Firestore -> SQLite. No live listener (per team decision) —
 * call sync() on launch and from pull-to-refresh in MyRequestsActivity.
 */
public class RequestSyncManager {

    public interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    private final DBHelper dbHelper;
    private final FirebaseFirestore firestore;

    public RequestSyncManager(DBHelper dbHelper) {
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
                        Payment p = doc.toObject(Payment.class);
                        p.setPaymentId(doc.getId());
                        dbHelper.upsertPayment(p);
                    }
                    if (callback != null) callback.onSyncComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSyncComplete(false);
                });
    }
}
