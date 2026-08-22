package com.example.techfix_mobile.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "techfix.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE repair_services (" +
                "service_id TEXT PRIMARY KEY, category_id TEXT, name TEXT, " +
                "description TEXT, price REAL, est_hours INTEGER)");

        db.execSQL("CREATE TABLE my_requests (" +
                "request_id TEXT PRIMARY KEY, service_id TEXT, category_id TEXT, " +
                "device_details TEXT, issue_desc TEXT, photo_local_path TEXT, photo_url TEXT, " +
                "branch_id TEXT, technician_id TEXT, status TEXT, " +
                "customer_lat REAL, customer_lng REAL, " +
                "requested_at INTEGER, completed_at INTEGER, " +
                "sync_status TEXT DEFAULT 'synced')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS repair_services");
        db.execSQL("DROP TABLE IF EXISTS my_requests");
        onCreate(db);
    }
}
