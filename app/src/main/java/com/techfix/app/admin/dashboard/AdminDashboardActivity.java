package com.techfix.app.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.techfix.app.R;
import com.techfix.app.admin.MockAdminSession;
import com.techfix.app.admin.resources.ManageResourceActivity;
import com.techfix.app.admin.resources.ResourceType;

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
    }
}