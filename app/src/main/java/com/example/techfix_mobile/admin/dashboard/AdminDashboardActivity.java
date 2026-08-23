package com.example.techfix_mobile.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.admin.MockAdminSession;
import com.example.techfix_mobile.admin.requests.IncomingRequestsActivity;
import com.example.techfix_mobile.admin.resources.ManageResourceActivity;
import com.example.techfix_mobile.admin.resources.ResourceType;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvPendingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + MockAdminSession.ADMIN_NAME);

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