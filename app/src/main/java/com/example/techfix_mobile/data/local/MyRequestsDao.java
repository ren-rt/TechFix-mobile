package com.example.techfix_mobile.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.techfix_mobile.model.RepairRequest;
import java.util.ArrayList;
import java.util.List;

public class MyRequestsDao {
    private DatabaseHelper dbHelper;

    public MyRequestsDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insert(RepairRequest request) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(request);
        db.insertWithOnConflict("my_requests", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateStatus(String requestId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("my_requests", values, "request_id = ?", new String[]{requestId});
    }

    public void updateSyncStatus(String requestId, String syncStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sync_status", syncStatus);
        db.update("my_requests", values, "request_id = ?", new String[]{requestId});
    }

    public List<RepairRequest> getAll() {
        List<RepairRequest> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("my_requests", null, null, null, null, null, "requested_at DESC");

        while (cursor.moveToNext()) {
            list.add(fromCursor(cursor));
        }
        cursor.close();
        return list;
    }

    public List<RepairRequest> getPendingSync() {
        List<RepairRequest> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("my_requests", null, "sync_status = ?",
                new String[]{"pending"}, null, null, null);

        while (cursor.moveToNext()) {
            list.add(fromCursor(cursor));
        }
        cursor.close();
        return list;
    }

    private ContentValues toContentValues(RepairRequest r) {
        ContentValues values = new ContentValues();
        values.put("request_id", r.getRequestId());
        values.put("service_id", r.getServiceId());
        values.put("category_id", r.getCategoryId());
        values.put("device_details", r.getDeviceDetails());
        values.put("issue_desc", r.getIssueDesc());
        values.put("photo_local_path", r.getPhotoLocalPath());
        values.put("photo_url", r.getDevicePhotoUrl());
        values.put("branch_id", r.getAssignedBranchId());
        values.put("technician_id", r.getAssignedTechnicianId());
        values.put("status", r.getStatus());
        values.put("customer_lat", r.getCustomerLat());
        values.put("customer_lng", r.getCustomerLng());
        values.put("requested_at", r.getRequestedAt());
        values.put("completed_at", r.getCompletedAt());
        values.put("sync_status", r.getSyncStatus());
        return values;
    }

    private RepairRequest fromCursor(Cursor cursor) {
        return new RepairRequest(
                cursor.getString(cursor.getColumnIndexOrThrow("request_id")),
                null, // customerId not stored locally — current_user table already tracks the logged-in user
                cursor.getString(cursor.getColumnIndexOrThrow("service_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("category_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("device_details")),
                cursor.getString(cursor.getColumnIndexOrThrow("issue_desc")),
                cursor.getString(cursor.getColumnIndexOrThrow("photo_local_path")),
                cursor.getString(cursor.getColumnIndexOrThrow("photo_url")),
                cursor.getString(cursor.getColumnIndexOrThrow("branch_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("technician_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("status")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("customer_lat")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("customer_lng")),
                cursor.getLong(cursor.getColumnIndexOrThrow("requested_at")),
                cursor.getLong(cursor.getColumnIndexOrThrow("completed_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("sync_status"))
        );
    }
}