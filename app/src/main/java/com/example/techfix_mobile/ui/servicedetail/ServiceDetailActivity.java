package com.example.techfix_mobile.ui.servicedetail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobile.R;

public class ServiceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "service_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_EST_HOURS = "est_hours";

    private String serviceId, categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        serviceId = getIntent().getStringExtra(EXTRA_SERVICE_ID);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        double price = getIntent().getDoubleExtra(EXTRA_PRICE, 0.0);
        int estHours = getIntent().getIntExtra(EXTRA_EST_HOURS, 0);

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvEstTime = findViewById(R.id.tvDetailEstTime);

        tvName.setText(name);
        tvDesc.setText(description);
        tvPrice.setText("LKR " + price);
        tvEstTime.setText("Estimated time: " + estHours + " hrs");

        findViewById(R.id.btnBookRepair).setOnClickListener(v -> {
            // TODO: open SubmitRequestActivity, pass serviceId + categoryId
            Toast.makeText(this, "Book repair tapped for: " + serviceId, Toast.LENGTH_SHORT).show();
        });
    }
}