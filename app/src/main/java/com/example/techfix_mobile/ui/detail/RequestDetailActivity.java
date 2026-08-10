package com.example.techfix_mobile.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobile.R;
// import com.example.techfix_mobile.data.PaymentRepository; // TODO: uncomment once Firebase is added
import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
// import com.example.techfix_mobile.ui.payment.PaymentActivity; // TODO: uncomment once PaymentActivity is added
// import com.example.techfix_mobile.ui.receipt.ReceiptActivity; // TODO: uncomment once ReceiptActivity is added

public class RequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "requestId";

    private DBHelper dbHelper;
    // private PaymentRepository paymentRepository; // TODO: uncomment once Firebase is added
    private RepairRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        dbHelper = new DBHelper(this);
        // paymentRepository = new PaymentRepository(); // TODO: uncomment once Firebase is added

        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        request = dbHelper.getRequestById(requestId);
        if (request == null) {
            finish();
            return;
        }

        bindTimeline();
        setupPayButton();
    }

    private void bindTimeline() {
        TextView title = findViewById(R.id.txtDetailTitle);
        TextView issue = findViewById(R.id.txtDetailIssue);
        TextView statusStep = findViewById(R.id.txtCurrentStatus);

        title.setText(request.getDeviceDetails());
        issue.setText(request.getIssueDesc());
        statusStep.setText(describeStatus(request.getStatus()));
    }

    private String describeStatus(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case RepairRequest.STATUS_PENDING: return "Pending — waiting for branch assignment";
            case RepairRequest.STATUS_ASSIGNED: return "Assigned to a technician";
            case RepairRequest.STATUS_IN_PROGRESS: return "Repair in progress";
            case RepairRequest.STATUS_COMPLETED: return "Repair completed — ready for payment";
            case RepairRequest.STATUS_READY_FOR_PICKUP: return "Ready for pickup";
            default: return status;
        }
    }

    private void setupPayButton() {
        Button payButton = findViewById(R.id.btnPay);

        if (!request.isPayable()) {
            payButton.setVisibility(android.view.View.GONE);
            return;
        }

        // TODO: uncomment this whole block once PaymentActivity/ReceiptActivity/Firebase exist.
        // For now just show the button so you can visually confirm it appears for
        // completed/ready_for_pickup status.
        payButton.setVisibility(android.view.View.VISIBLE);
        payButton.setText("Pay Now (not wired up yet)");
        payButton.setEnabled(false);

        /*
        Payment existing = dbHelper.getPaymentForRequest(request.getRequestId());
        if (existing != null && Payment.STATUS_COMPLETED.equals(existing.getStatus())) {
            payButton.setText("View Receipt");
            payButton.setOnClickListener(v -> {
                Intent i = new Intent(this, ReceiptActivity.class);
                i.putExtra("paymentId", existing.getPaymentId());
                startActivity(i);
            });
            return;
        }

        payButton.setVisibility(android.view.View.VISIBLE);
        payButton.setOnClickListener(v -> {
            payButton.setEnabled(false);
            paymentRepository.hasExistingPayment(request.getRequestId(), exists -> {
                payButton.setEnabled(true);
                if (exists) {
                    android.widget.Toast.makeText(this,
                            "A payment for this request is already pending", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(this, PaymentActivity.class);
                i.putExtra("requestId", request.getRequestId());
                i.putExtra("amount", 0.0);
                startActivity(i);
            });
        });
        */
    }
}