package com.techfix.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.techfix.app.R;
import com.techfix.app.admin.dashboard.AdminDashboardActivity;

// TEMPORARY placeholder — Person 1 will replace this with real Splash/Login logic.
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, AdminDashboardActivity.class));
        finish();
    }
}