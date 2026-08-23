package com.example.techfix_mobile.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.model.RepairService;
import java.util.ArrayList;
import java.util.List;

public class RepairServiceDao {
    private DatabaseHelper dbHelper;

    public RepairServiceDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insert(RepairService service) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("service_id", service.getServiceId());
        values.put("category_id", service.getCategoryId());
        values.put("name", service.getName());
        values.put("description", service.getDescription());
        values.put("price", service.getPrice());
        values.put("est_hours", service.getEstHours());
        db.insertWithOnConflict("repair_services", null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<RepairService> getAll() {
        List<RepairService> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("repair_services", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            RepairService service = new RepairService(
                    cursor.getString(cursor.getColumnIndexOrThrow("service_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("est_hours"))
            );
            list.add(service);
        }
        cursor.close();
        return list;
    }

    public RepairService getById(String serviceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("repair_services", null, "service_id = ?",
                new String[]{serviceId}, null, null, null);

        RepairService service = null;
        if (cursor.moveToFirst()) {
            service = new RepairService(
                    cursor.getString(cursor.getColumnIndexOrThrow("service_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("est_hours"))
            );
        }
        cursor.close();
        return service;
    }
}
