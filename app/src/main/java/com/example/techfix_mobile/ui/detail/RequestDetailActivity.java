package com.example.techfix_mobile.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.PaymentRepository;
import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.ui.payment.PaymentActivity;
import com.example.techfix_mobile.ui.receipt.ReceiptActivity;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "requestId";

    private DBHelper dbHelper;
    private PaymentRepository paymentRepository;
    private RepairRequest request;
    private Payment existingPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        dbHelper = new DBHelper(this);
        paymentRepository = new PaymentRepository();

        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        request = dbHelper.getRequestById(requestId);
        if (request == null) {
            finish();
            return;
        }

        existingPayment = dbHelper.getPaymentForRequest(request.getRequestId());

        bindTimeline();
        setupPayButton();
    }

    private void bindTimeline() {
        TextView title = findViewById(R.id.txtDetailTitle);
        TextView issue = findViewById(R.id.txtDetailIssue);
        TextView statusStep = findViewById(R.id.txtCurrentStatus);

        title.setText(request.getDeviceDetails());
        issue.setText(request.getIssueDesc());
        statusStep.setText(describeStatus());
    }

    /** Combines repair status + payment status into one accurate line, instead of
     *  showing "ready for payment" even after payment is already done. */
    private String describeStatus() {
        boolean paid = existingPayment != null && Payment.STATUS_COMPLETED.equals(existingPayment.getStatus());
        String status = request.getStatus();
        if (status == null) return "Unknown";

        switch (status) {
            case RepairRequest.STATUS_PENDING: return "Pending — waiting for branch assignment";
            case RepairRequest.STATUS_ASSIGNED: return "Assigned to a technician";
            case RepairRequest.STATUS_IN_PROGRESS: return "Repair in progress";
            case RepairRequest.STATUS_COMPLETED:
                return paid ? "Repair completed — paid" : "Repair completed — ready for payment";
            case RepairRequest.STATUS_READY_FOR_PICKUP:
                return paid ? "Ready for pickup — paid" : "Ready for pickup — payment due";
            default: return status;
        }
    }

    private void setupPayButton() {
        Button payButton = findViewById(R.id.btnPay);

        if (!request.isPayable()) {
            payButton.setVisibility(android.view.View.GONE);
            return;
        }

        if (existingPayment != null && Payment.STATUS_COMPLETED.equals(existingPayment.getStatus())) {
            payButton.setVisibility(android.view.View.VISIBLE);
            payButton.setText("View Receipt");
            payButton.setEnabled(true);
            payButton.setOnClickListener(v -> {
                Intent i = new Intent(this, ReceiptActivity.class);
                i.putExtra("paymentId", existingPayment.getPaymentId());
                startActivity(i);
            });
            return;
        }

        payButton.setVisibility(android.view.View.VISIBLE);
        payButton.setText("Pay Now");
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
                i.putExtra("amount", 1500.0); // TODO: replace with repairServices.basePrice once Person 2's data exists
                startActivity(i);
            });
        });
    }
}