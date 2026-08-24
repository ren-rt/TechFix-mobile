package com.example.techfix_mobile.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.LoginActivity;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.admin.requests.IncomingRequestsActivity;
import com.example.techfix_mobile.admin.resources.ManageResourceActivity;
import com.example.techfix_mobile.admin.resources.ResourceType;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvPendingCount;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome");
        loadAdminName();

        tvPendingCount = findViewById(R.id.tvPendingCount);

        CardView cardIncomingRequests = findViewById(R.id.cardIncomingRequests);
        cardIncomingRequests.setOnClickListener(v ->
                startActivity(new Intent(this, IncomingRequestsActivity.class)));

        findViewById(R.id.cardBranches).setOnClickListener(v -> openManage(ResourceType.BRANCH));
        findViewById(R.id.cardTechnicians).setOnClickListener(v -> openManage(ResourceType.TECHNICIAN));
        findViewById(R.id.cardParts).setOnClickListener(v -> openManage(ResourceType.PART));
        findViewById(R.id.cardCategories).setOnClickListener(v -> openManage(ResourceType.CATEGORY));
        findViewById(R.id.cardServices).setOnClickListener(v -> openManage(ResourceType.SERVICE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            new DatabaseHelper(this).clearUserData();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAdminName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            tvWelcome.setText("Welcome, Admin");
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    tvWelcome.setText("Welcome, " + (name != null ? name : "Admin"));
                })
                .addOnFailureListener(e -> tvWelcome.setText("Welcome, Admin"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingCount();
    }

    private void loadPendingCount() {
        FirebaseFirestore.getInstance().collection("repairRequests")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = snapshot.size();
                    tvPendingCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> tvPendingCount.setText("—"));
    }

    private void openManage(ResourceType type) {
        Intent i = new Intent(this, ManageResourceActivity.class);
        i.putExtra(ManageResourceActivity.EXTRA_TYPE, type.name());
        startActivity(i);
    }
}