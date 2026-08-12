package com.example.techfix_mobile.ui.home;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.remote.RepairFirestoreRepository;
import com.example.techfix_mobile.model.RepairService;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private final List<RepairService> serviceList = new ArrayList<>();
    private final RepairFirestoreRepository repository = new RepairFirestoreRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ServiceAdapter(serviceList, service -> {
            // TODO: open ServiceDetailActivity, pass service.getServiceId()
            Toast.makeText(this, "Clicked: " + service.getName(), Toast.LENGTH_SHORT).show();
        });
        rvServices.setAdapter(adapter);

        loadServices();
    }

    private void loadServices() {
        repository.fetchAllServices(new RepairFirestoreRepository.OnServicesLoaded() {
            @Override
            public void onLoaded(List<RepairService> services) {
                serviceList.clear();
                serviceList.addAll(services);
                adapter.notifyDataSetChanged();

                if (services.isEmpty()) {
                    Toast.makeText(HomeActivity.this,
                            "No repair services found yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this,
                        "Failed to load services: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}