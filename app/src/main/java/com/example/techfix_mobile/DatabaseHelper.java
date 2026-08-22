package com.example.techfix_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "techfix.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    // Current User Table
    public static final String TABLE_CURRENT_USER = "current_user";
    public static final String COL_UID = "uid";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_CURRENT_USER + "("
                + COL_UID + " TEXT PRIMARY KEY,"
                + COL_NAME + " TEXT,"
                + COL_EMAIL + " TEXT,"
                + COL_PHONE + " TEXT,"
                + COL_ROLE + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        // Branches Table with contact_number
        String CREATE_BRANCHES_TABLE = "CREATE TABLE branches (" +
                "branch_id TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "address TEXT, " +
                "lat REAL, " +
                "lng REAL, " +
                "phone TEXT, " +
                "contact_number TEXT)";
        db.execSQL(CREATE_BRANCHES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE branches ADD COLUMN contact_number TEXT");
        }
    }

    public void saveUser(String uid, String name, String email, String phone, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_UID, uid);
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PHONE, phone);
        values.put(COL_ROLE, role);
        db.insertWithOnConflict(TABLE_CURRENT_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getCurrentUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CURRENT_USER, null, null, null, null, null, null);
    }

    public void clearUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CURRENT_USER, null, null);
    }
}