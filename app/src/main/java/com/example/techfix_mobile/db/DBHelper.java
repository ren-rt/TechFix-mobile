package com.example.techfix_mobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "techfix.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_MY_REQUESTS = "my_requests";
    public static final String TABLE_PAYMENTS_CACHE = "payments_cache";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MY_REQUESTS + " (" +
                "request_id TEXT PRIMARY KEY, service_id TEXT, category_id TEXT, " +
                "device_details TEXT, issue_desc TEXT, photo_local_path TEXT, photo_url TEXT, " +
                "branch_id TEXT, technician_id TEXT, status TEXT, " +
                "customer_lat REAL, customer_lng REAL, " +
                "requested_at INTEGER, completed_at INTEGER, " +
                "sync_status TEXT DEFAULT 'synced')");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PAYMENTS_CACHE + " (" +
                "payment_id TEXT PRIMARY KEY, request_id TEXT, customer_id TEXT, " +
                "amount REAL, currency TEXT, status TEXT, payhere_payment_id TEXT, " +
                "method TEXT, paid_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for coursework — bump DB_VERSION and drop/recreate if schema changes.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS_CACHE);
        onCreate(db);
    }

    // ---------- my_requests ----------

    public void upsertRequest(RepairRequest r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("request_id", r.getRequestId());
        cv.put("service_id", r.getServiceId());
        cv.put("category_id", r.getCategoryId());
        cv.put("device_details", r.getDeviceDetails());
        cv.put("issue_desc", r.getIssueDesc());
        cv.put("photo_url", r.getDevicePhotoUrl());
        cv.put("branch_id", r.getAssignedBranchId());
        cv.put("technician_id", r.getAssignedTechnicianId());
        cv.put("status", r.getStatus());
        cv.put("customer_lat", r.getCustomerLat());
        cv.put("customer_lng", r.getCustomerLng());
        cv.put("requested_at", r.getRequestedAt());
        cv.put("completed_at", r.getCompletedAt());
        cv.put("sync_status", r.getSyncStatus());
        db.insertWithOnConflict(TABLE_MY_REQUESTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<RepairRequest> getAllRequests() {
        return queryRequests(null, null);
    }

    /** Repair History = anything already completed or picked up. */
    public List<RepairRequest> getHistoryRequests() {
        return queryRequests("status IN (?, ?)",
                new String[]{RepairRequest.STATUS_COMPLETED, RepairRequest.STATUS_READY_FOR_PICKUP});
    }

    public RepairRequest getRequestById(String requestId) {
        List<RepairRequest> results = queryRequests("request_id = ?", new String[]{requestId});
        return results.isEmpty() ? null : results.get(0);
    }

    private List<RepairRequest> queryRequests(String selection, String[] args) {
        List<RepairRequest> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MY_REQUESTS, null, selection, args, null, null, "requested_at DESC");
        while (c.moveToNext()) {
            RepairRequest r = new RepairRequest();
            r.setRequestId(c.getString(c.getColumnIndexOrThrow("request_id")));
            r.setServiceId(c.getString(c.getColumnIndexOrThrow("service_id")));
            r.setCategoryId(c.getString(c.getColumnIndexOrThrow("category_id")));
            r.setDeviceDetails(c.getString(c.getColumnIndexOrThrow("device_details")));
            r.setIssueDesc(c.getString(c.getColumnIndexOrThrow("issue_desc")));
            r.setDevicePhotoUrl(c.getString(c.getColumnIndexOrThrow("photo_url")));
            r.setAssignedBranchId(c.getString(c.getColumnIndexOrThrow("branch_id")));
            r.setAssignedTechnicianId(c.getString(c.getColumnIndexOrThrow("technician_id")));
            r.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
            r.setCustomerLat(c.getDouble(c.getColumnIndexOrThrow("customer_lat")));
            r.setCustomerLng(c.getDouble(c.getColumnIndexOrThrow("customer_lng")));
            r.setRequestedAt(c.getLong(c.getColumnIndexOrThrow("requested_at")));
            r.setCompletedAt(c.getLong(c.getColumnIndexOrThrow("completed_at")));
            r.setSyncStatus(c.getString(c.getColumnIndexOrThrow("sync_status")));
            list.add(r);
        }
        c.close();
        return list;
    }

    // ---------- payments_cache ----------

    public void upsertPayment(Payment p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("payment_id", p.getPaymentId());
        cv.put("request_id", p.getRequestId());
        cv.put("customer_id", p.getCustomerId());
        cv.put("amount", p.getAmount());
        cv.put("currency", p.getCurrency());
        cv.put("status", p.getStatus());
        cv.put("payhere_payment_id", p.getPayherePaymentId());
        cv.put("method", p.getMethod());
        cv.put("paid_at", p.getPaidAt());
        db.insertWithOnConflict(TABLE_PAYMENTS_CACHE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Payment getPaymentForRequest(String requestId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PAYMENTS_CACHE, null, "request_id = ?",
                new String[]{requestId}, null, null, "paid_at DESC", "1");
        Payment p = null;
        if (c.moveToFirst()) {
            p = new Payment();
            p.setPaymentId(c.getString(c.getColumnIndexOrThrow("payment_id")));
            p.setRequestId(c.getString(c.getColumnIndexOrThrow("request_id")));
            p.setCustomerId(c.getString(c.getColumnIndexOrThrow("customer_id")));
            p.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
            p.setCurrency(c.getString(c.getColumnIndexOrThrow("currency")));
            p.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
            p.setPayherePaymentId(c.getString(c.getColumnIndexOrThrow("payhere_payment_id")));
            p.setMethod(c.getString(c.getColumnIndexOrThrow("method")));
            p.setPaidAt(c.getLong(c.getColumnIndexOrThrow("paid_at")));
        }
        c.close();
        return p;
    }
}
