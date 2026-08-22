package com.example.techfix_mobile;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText profileName, profilePhone;
    private TextView profileEmail;
    private Button updateProfileBtn;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        profileName = findViewById(R.id.profileName);
        profilePhone = findViewById(R.id.profilePhone);
        profileEmail = findViewById(R.id.profileEmail);
        updateProfileBtn = findViewById(R.id.updateProfileBtn);

        loadProfileData();

        updateProfileBtn.setOnClickListener(v -> updateProfile());
    }

    private void loadProfileData() {
        Cursor cursor = dbHelper.getCurrentUser();
        if (cursor != null && cursor.moveToFirst()) {
            profileName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
            profilePhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE)));
            profileEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)));
            cursor.close();
        }
    }

    private void updateProfile() {
        String name = profileName.getText().toString().trim();
        String phone = profilePhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update Local SQLite as well
                    updateLocalProfile(name, phone);
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLocalProfile(String name, String phone) {
        // Simple way: re-save the whole user from what we know
        Cursor cursor = dbHelper.getCurrentUser();
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
            dbHelper.saveUser(uid, name, email, phone, role);
            cursor.close();
        }
    }
}