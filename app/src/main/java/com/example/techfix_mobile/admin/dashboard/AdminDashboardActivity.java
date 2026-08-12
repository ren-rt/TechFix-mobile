package com.example.techfix_mobile.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.admin.MockAdminSession;
import com.example.techfix_mobile.admin.resources.ManageResourceActivity;
import com.example.techfix_mobile.admin.resources.ResourceType;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + MockAdminSession.ADMIN_NAME);

        Button btnManageBranches = findViewById(R.id.btnManageBranches);
        btnManageBranches.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageResourceActivity.class);
            i.putExtra(ManageResourceActivity.EXTRA_TYPE, ResourceType.BRANCH.name());
            startActivity(i);
        });

        Button btnManageTechnicians = findViewById(R.id.btnManageTechnicians);
        btnManageTechnicians.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageResourceActivity.class);
            i.putExtra(ManageResourceActivity.EXTRA_TYPE, ResourceType.TECHNICIAN.name());
            startActivity(i);
        });

        Button btnManageParts = findViewById(R.id.btnManageParts);
        btnManageParts.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageResourceActivity.class);
            i.putExtra(ManageResourceActivity.EXTRA_TYPE, ResourceType.PART.name());
            startActivity(i);
        });

        Button btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageCategories.setOnClickListener(v -> {
            Intent i = new Intent(this, ManageResourceActivity.class);
            i.putExtra(ManageResourceActivity.EXTRA_TYPE, ResourceType.CATEGORY.name());
            startActivity(i);
        });

        Button btnIncomingRequests = findViewById(R.id.btnIncomingRequests);
        btnIncomingRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.techfix_mobile.admin.requests.IncomingRequestsActivity.class));
        });
    }
}