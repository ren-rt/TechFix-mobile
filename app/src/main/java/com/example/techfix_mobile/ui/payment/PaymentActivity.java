package com.example.techfix_mobile.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.PaymentRepository;
import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.Payment;
//import com.example.techfix_mobile.ui.receipt.ReceiptActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

/**
 * Requires (in app/build.gradle):
 *   implementation 'com.github.PayHereDevs:payhere-android-sdk:v3.0.18'
 *   implementation 'androidx.appcompat:appcompat:1.6.0'
 *   implementation 'com.google.code.gson:gson:2.8.0'
 * and in settings.gradle repositories: maven { url "https://jitpack.io" }
 *
 * TODO: replace PAYHERE_MERCHANT_ID with the real sandbox merchant ID once created.
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private static final int PAYHERE_REQUEST = 11001;

    // TODO: fill in with your sandbox Merchant ID from the PayHere dashboard.
    private static final String PAYHERE_MERCHANT_ID = "1237421";
    // Points at the deployed payhereNotify Cloud Function.
    private static final String NOTIFY_URL = "REPLACE_WITH_CLOUD_FUNCTION_URL";

    private DBHelper dbHelper;
    private PaymentRepository paymentRepository;
    private String requestId;
    private double amount;
    private String pendingOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dbHelper = new DBHelper(this);
        paymentRepository = new PaymentRepository();

        requestId = getIntent().getStringExtra("requestId");
        amount = getIntent().getDoubleExtra("amount", 0.0);

        startPaymentFlow();
    }

    private void startPaymentFlow() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Step 1: create the Firestore payments doc BEFORE launching PayHere, so the
        // webhook always has a document to update even if the SDK/app is killed mid-payment.
        paymentRepository.createPendingPayment(requestId, user.getUid(), amount, new PaymentRepository.OnOrderCreated() {
            @Override
            public void onCreated(String orderId) {
                pendingOrderId = orderId;
                launchPayHere(orderId, user);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to create pending payment", e);
                Toast.makeText(PaymentActivity.this, "Could not start payment, try again", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void launchPayHere(String orderId, FirebaseUser user) {
        InitRequest req = new InitRequest();
        req.setMerchantId(PAYHERE_MERCHANT_ID);
        req.setCurrency("LKR");
        req.setAmount(amount);
        req.setOrderId(orderId);
        req.setItemsDescription("TechFix repair service - " + requestId);
        req.setNotifyUrl(NOTIFY_URL);

        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Customer";
        String[] nameParts = displayName.split(" ", 2);
        req.getCustomer().setFirstName(nameParts[0]);
        req.getCustomer().setLastName(nameParts.length > 1 ? nameParts[1] : "");
        req.getCustomer().setEmail(user.getEmail() != null ? user.getEmail() : "");
        req.getCustomer().setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        // TODO: pull real address from the branch/customer profile if PayHere requires it.
        req.getCustomer().getAddress().setAddress("N/A");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // switch to live URL for production
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PAYHERE_REQUEST) return;

        if (data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response =
                    (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            Log.d(TAG, "PayHere result: " + response);
        }

        if (resultCode == Activity.RESULT_OK) {
            // SDK thinks it went through, but the Cloud Function write is the source of
            // truth (per the handover doc). Poll the Firestore doc briefly.
            Toast.makeText(this, "Confirming payment...", Toast.LENGTH_SHORT).show();
            pollPaymentStatus(0);
        } else {
            Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Polls the payments doc a few times since the webhook write can lag slightly behind the SDK callback. */
    private void pollPaymentStatus(int attempt) {
        if (pendingOrderId == null) return;
        paymentRepository.checkPaymentStatus(pendingOrderId, payment -> {
            if (payment != null && Payment.STATUS_COMPLETED.equals(payment.getStatus())) {
                dbHelper.upsertPayment(payment);
                // TODO: once ReceiptActivity is added, replace this with:
                // Intent i = new Intent(this, ReceiptActivity.class);
                // i.putExtra("paymentId", payment.getPaymentId());
                // startActivity(i);
                android.widget.Toast.makeText(this, "Payment successful! Receipt screen coming soon.", android.widget.Toast.LENGTH_LONG).show();
                finish();
            } else if (payment != null && Payment.STATUS_FAILED.equals(payment.getStatus())) {
                Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
                finish();
            } else if (attempt < 5) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> pollPaymentStatus(attempt + 1), 2000);
            } else {
                Toast.makeText(this, "Still confirming — check My Requests shortly", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
