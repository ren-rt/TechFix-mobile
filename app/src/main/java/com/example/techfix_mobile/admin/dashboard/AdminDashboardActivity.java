package com.example.techfix_mobile.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.admin.requests.IncomingRequestsActivity;
import com.example.techfix_mobile.admin.resources.ManageResourceActivity;
import com.example.techfix_mobile.admin.resources.ResourceType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvPendingCount;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

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