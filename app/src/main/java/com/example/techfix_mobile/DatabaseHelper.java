package com.example.techfix_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "techfix.db";
    private static final int DATABASE_VERSION = 3; // bumped: merges in my_requests + payments_cache

    public static final String TABLE_CURRENT_USER = "current_user";
    public static final String COL_UID = "uid";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role";

    public static final String TABLE_MY_REQUESTS = "my_requests";
    public static final String TABLE_PAYMENTS_CACHE = "payments_cache";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CURRENT_USER + "(" +
                COL_UID + " TEXT PRIMARY KEY," +
                COL_NAME + " TEXT," +
                COL_EMAIL + " TEXT," +
                COL_PHONE + " TEXT," +
                COL_ROLE + " TEXT)");

        db.execSQL("CREATE TABLE branches (" +
                "branch_id TEXT PRIMARY KEY, name TEXT, address TEXT, " +
                "lat REAL, lng REAL, phone TEXT, contact_number TEXT)");

        db.execSQL("CREATE TABLE repair_services (" +
                "service_id TEXT PRIMARY KEY, category_id TEXT, name TEXT, " +
                "description TEXT, price REAL, est_hours INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_MY_REQUESTS + " (" +
                "request_id TEXT PRIMARY KEY, service_id TEXT, category_id TEXT, " +
                "device_details TEXT, issue_desc TEXT, photo_local_path TEXT, photo_url TEXT, " +
                "branch_id TEXT, technician_id TEXT, status TEXT, " +
                "customer_lat REAL, customer_lng REAL, " +
                "requested_at INTEGER, completed_at INTEGER, " +
                "sync_status TEXT DEFAULT 'synced')");

        db.execSQL("CREATE TABLE " + TABLE_PAYMENTS_CACHE + " (" +
                "payment_id TEXT PRIMARY KEY, request_id TEXT, customer_id TEXT, " +
                "amount REAL, currency TEXT, status TEXT, payhere_payment_id TEXT, " +
                "method TEXT, paid_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Dev-phase migration: wipe and recreate. Fine for now — uninstall/clear app
        // data on your test device once after pulling this change.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_USER);
        db.execSQL("DROP TABLE IF EXISTS branches");
        db.execSQL("DROP TABLE IF EXISTS repair_services");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS_CACHE);
        onCreate(db);
    }

    // ---------- current_user (Person 1) ----------
    public void saveUser(String uid, String name, String email, String phone, String role) {
        ContentValues v = new ContentValues();
        v.put(COL_UID, uid); v.put(COL_NAME, name); v.put(COL_EMAIL, email);
        v.put(COL_PHONE, phone); v.put(COL_ROLE, role);
        getWritableDatabase().insertWithOnConflict(TABLE_CURRENT_USER, null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public Cursor getCurrentUser() {
        return getReadableDatabase().query(TABLE_CURRENT_USER, null, null, null, null, null, null);
    }
    public void clearUserData() {
        getWritableDatabase().delete(TABLE_CURRENT_USER, null, null);
    }

    // ---------- my_requests (Person 3's API — RequestSyncManager/PaymentActivity/etc use these) ----------
    public void upsertRequest(RepairRequest r) {
        ContentValues cv = new ContentValues();
        cv.put("request_id", r.getRequestId());
        cv.put("service_id", r.getServiceId());
        cv.put("category_id", r.getCategoryId());
        cv.put("device_details", r.getDeviceDetails());
        cv.put("issue_desc", r.getIssueDesc());
        cv.put("photo_local_path", r.getPhotoLocalPath());
        cv.put("photo_url", r.getDevicePhotoUrl());
        cv.put("branch_id", r.getAssignedBranchId());
        cv.put("technician_id", r.getAssignedTechnicianId());
        cv.put("status", r.getStatus());
        cv.put("customer_lat", r.getCustomerLat());
        cv.put("customer_lng", r.getCustomerLng());
        cv.put("requested_at", r.getRequestedAt());
        cv.put("completed_at", r.getCompletedAt());
        cv.put("sync_status", r.getSyncStatus());
        getWritableDatabase().insertWithOnConflict(TABLE_MY_REQUESTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public List<RepairRequest> getAllRequests() { return queryRequests(null, null); }
    public List<RepairRequest> getHistoryRequests() {
        return queryRequests("status IN (?, ?)",
                new String[]{RepairRequest.STATUS_COMPLETED, RepairRequest.STATUS_READY_FOR_PICKUP});
    }
    public RepairRequest getRequestById(String requestId) {
        List<RepairRequest> r = queryRequests("request_id = ?", new String[]{requestId});
        return r.isEmpty() ? null : r.get(0);
    }
    private List<RepairRequest> queryRequests(String selection, String[] args) {
        List<RepairRequest> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_MY_REQUESTS, null, selection, args, null, null, "requested_at DESC");
        while (c.moveToNext()) {
            RepairRequest r = new RepairRequest();
            r.setRequestId(c.getString(c.getColumnIndexOrThrow("request_id")));
            r.setServiceId(c.getString(c.getColumnIndexOrThrow("service_id")));
            r.setCategoryId(c.getString(c.getColumnIndexOrThrow("category_id")));
            r.setDeviceDetails(c.getString(c.getColumnIndexOrThrow("device_details")));
            r.setIssueDesc(c.getString(c.getColumnIndexOrThrow("issue_desc")));
            r.setPhotoLocalPath(c.getString(c.getColumnIndexOrThrow("photo_local_path")));
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
        getWritableDatabase().insertWithOnConflict(TABLE_PAYMENTS_CACHE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
    public Payment getPaymentForRequest(String requestId) {
        Cursor c = getReadableDatabase().query(TABLE_PAYMENTS_CACHE, null, "request_id = ?",
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