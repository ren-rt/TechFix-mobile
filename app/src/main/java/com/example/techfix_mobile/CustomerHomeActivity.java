package com.example.techfix_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        Button viewBranchesBtn = findViewById(R.id.viewBranchesBtn);
        Button profileBtn = findViewById(R.id.profileBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        viewBranchesBtn.setOnClickListener(v -> {
            startActivity(new Intent(CustomerHomeActivity.this, BranchListActivity.class));
        });

        profileBtn.setOnClickListener(v -> {
            startActivity(new Intent(CustomerHomeActivity.this, ProfileActivity.class));
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            DatabaseHelper dbHelper = new DatabaseHelper(CustomerHomeActivity.this);
            dbHelper.clearUserData();
            startActivity(new Intent(CustomerHomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}