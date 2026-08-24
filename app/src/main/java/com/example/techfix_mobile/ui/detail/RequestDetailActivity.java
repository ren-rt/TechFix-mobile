package com.example.techfix_mobile.ui.detail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.PaymentRepository;
import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.ui.payment.PaymentActivity;
import com.example.techfix_mobile.ui.receipt.ReceiptActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "requestId";

    private DatabaseHelper dbHelper;
    private PaymentRepository paymentRepository;
    private FirebaseFirestore firestore;
    private RepairRequest request;
    private Payment existingPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        dbHelper = new DatabaseHelper(this);
        paymentRepository = new PaymentRepository();
        firestore = FirebaseFirestore.getInstance();

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
        ImageView photo = findViewById(R.id.imgDevicePhoto);

        title.setText(request.getDeviceDetails());
        issue.setText(request.getIssueDesc());
        statusStep.setText(describeStatus());

        String photoUrl = request.getDevicePhotoUrl();
        if (photoUrl != null && photoUrl.startsWith("data:image")) {
            try {
                String base64 = photoUrl.substring(photoUrl.indexOf(',') + 1);
                byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    photo.setImageBitmap(bmp);
                    photo.setVisibility(android.view.View.VISIBLE);
                }
            } catch (Exception e) {
                photo.setVisibility(android.view.View.GONE);
            }
        }
    }

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
                if (exists) {
                    payButton.setEnabled(true);
                    Toast.makeText(this,
                            "A payment for this request is already pending", Toast.LENGTH_LONG).show();
                    return;
                }
                fetchServicePriceAndLaunchPayment(payButton);
            });
        });
    }

    private void fetchServicePriceAndLaunchPayment(Button payButton) {
        String serviceId = request.getServiceId();
        if (serviceId == null || serviceId.isEmpty()) {
            payButton.setEnabled(true);
            Toast.makeText(this, "This request has no linked service — can't determine price", Toast.LENGTH_LONG).show();
            return;
        }

        firestore.collection("repairServices").document(serviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    payButton.setEnabled(true);
                    if (!doc.exists()) {
                        Toast.makeText(this, "Service details not found", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Double basePrice = doc.getDouble("basePrice");
                    if (basePrice == null || basePrice <= 0) {
                        Toast.makeText(this, "Invalid service price", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent i = new Intent(this, PaymentActivity.class);
                    i.putExtra("requestId", request.getRequestId());
                    i.putExtra("amount", basePrice);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    payButton.setEnabled(true);
                    Toast.makeText(this, "Could not load service price: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
