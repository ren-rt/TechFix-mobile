package com.example.techfix_mobile.ui.confirmation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.ui.home.HomeActivity;
import com.example.techfix_mobile.ui.myrequests.MyRequestsActivity;

public class ConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_BRANCH_NAME = "branch_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        String branchName = getIntent().getStringExtra(EXTRA_BRANCH_NAME);

        TextView tvBranch = findViewById(R.id.tvBranchName);
        TextView tvRequestId = findViewById(R.id.tvRequestId);

        tvBranch.setText("Assigned to: " + branchName);
        tvRequestId.setText("Request ID: " + requestId);

        findViewById(R.id.btnViewMyRequests).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRequestsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}