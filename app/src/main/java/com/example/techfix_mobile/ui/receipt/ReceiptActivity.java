package com.example.techfix_mobile.ui.receipt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.db.DBHelper;
import com.example.techfix_mobile.model.Payment;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.util.PdfReceiptGenerator;

import java.io.File;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Payment payment;
    private RepairRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        dbHelper = new DBHelper(this);
        String paymentId = getIntent().getStringExtra("paymentId");

        // Look up via the request the payment belongs to. If you added a direct
        // getPaymentById to DBHelper, swap this for that — kept simple here.
        request = null;
        payment = null;
        for (RepairRequest r : dbHelper.getAllRequests()) {
            Payment p = dbHelper.getPaymentForRequest(r.getRequestId());
            if (p != null && paymentId.equals(p.getPaymentId())) {
                payment = p;
                request = r;
                break;
            }
        }

        if (payment == null) {
            Toast.makeText(this, "Receipt not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
    }

    private void bindViews() {
        TextView receiptNo = findViewById(R.id.txtReceiptNo);
        TextView deviceText = findViewById(R.id.txtReceiptDevice);
        TextView amountText = findViewById(R.id.txtReceiptAmount);
        TextView statusText = findViewById(R.id.txtReceiptStatus);
        Button downloadButton = findViewById(R.id.btnDownloadPdf);

        receiptNo.setText("Receipt #" + payment.getPaymentId());
        deviceText.setText(request != null ? request.getDeviceDetails() : "");
        amountText.setText(String.format(Locale.getDefault(), "%s %.2f", payment.getCurrency(), payment.getAmount()));
        statusText.setText(payment.getStatus());

        downloadButton.setOnClickListener(v -> downloadPdf());
    }

    private void downloadPdf() {
        String deviceDetails = request != null ? request.getDeviceDetails() : null;
        File pdfFile = PdfReceiptGenerator.generate(this, payment, deviceDetails);
        if (pdfFile == null) {
            Toast.makeText(this, "Could not generate PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        // Requires a FileProvider declared in AndroidManifest.xml — see README for the snippet.
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Saved to " + pdfFile.getAbsolutePath() + " (no PDF viewer found)", Toast.LENGTH_LONG).show();
        }
    }
}
