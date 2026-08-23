package com.example.techfix_mobile;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    verifyProfileAndRoute(currentUser.getUid());
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }, 2000);
    }

    private void verifyProfileAndRoute(String uid) {
        // Try to fetch from Firestore to satisfy Test Case 3.1
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String role = document.getString("role");
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String phone = document.getString("phone");

                    // Refresh local cache
                    dbHelper.saveUser(uid, name, email, phone, role);

                    if ("admin".equals(role)) {
                        startActivity(new Intent(SplashActivity.this, AdminHomeActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, CustomerHomeActivity.class));
                    }
                    finish();
                } else {
                    // Test Case 3.1: Profile doc missing
                    handleMissingProfile();
                }
            } else {
                // Network error or offline
                // Fallback to local cache for Test Case 3.3
                routeFromLocalCache();
            }
        });
    }

    private void handleMissingProfile() {
        Toast.makeText(this, "Couldn't load your profile - please sign in again", Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
        dbHelper.clearUserData();
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

    private void routeFromLocalCache() {
        Cursor cursor = dbHelper.getCurrentUser();
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE));
            cursor.close();
            if ("admin".equals(role)) {
                startActivity(new Intent(SplashActivity.this, AdminHomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, CustomerHomeActivity.class));
            }
            finish();
        } else {
            // No local cache either, must login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }
}