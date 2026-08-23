package com.example.techfix_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Button logoutBtn = findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            DatabaseHelper dbHelper = new DatabaseHelper(AdminHomeActivity.this);
            dbHelper.clearUserData();
            startActivity(new Intent(AdminHomeActivity.this, LoginActivity.class));
            finish();
        });
        
        // Other admin buttons would be handled by Person 4
    }
}