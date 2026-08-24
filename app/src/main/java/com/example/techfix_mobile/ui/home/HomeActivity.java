package com.example.techfix_mobile.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobile.BranchListActivity;
import com.example.techfix_mobile.DatabaseHelper;
import com.example.techfix_mobile.LoginActivity;
import com.example.techfix_mobile.ProfileActivity;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.remote.RepairFirestoreRepository;
import com.example.techfix_mobile.model.DeviceCategory;
import com.example.techfix_mobile.model.RepairService;
import com.example.techfix_mobile.ui.servicedetail.ServiceDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private final List<RepairService> allServices = new ArrayList<>();
    private final List<RepairService> displayedServices = new ArrayList<>();
    private final RepairFirestoreRepository repository = new RepairFirestoreRepository();

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private final List<DeviceCategory> categoryList = new ArrayList<>();

    private EditText etSearch;
    private String selectedCategoryId = null; // null = no category filter (show all)
    private String currentSearchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnBranches = findViewById(R.id.btnBranches);
        btnBranches.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, BranchListActivity.class)));

        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ServiceAdapter(displayedServices, service -> {
            Intent intent = new Intent(HomeActivity.this, ServiceDetailActivity.class);
            intent.putExtra(ServiceDetailActivity.EXTRA_SERVICE_ID, service.getServiceId());
            intent.putExtra(ServiceDetailActivity.EXTRA_CATEGORY_ID, service.getCategoryId());
            intent.putExtra(ServiceDetailActivity.EXTRA_NAME, service.getName());
            intent.putExtra(ServiceDetailActivity.EXTRA_DESCRIPTION, service.getDescription());
            intent.putExtra(ServiceDetailActivity.EXTRA_PRICE, service.getPrice());
            intent.putExtra(ServiceDetailActivity.EXTRA_EST_HOURS, service.getEstHours());
            startActivity(intent);
        });
        rvServices.setAdapter(adapter);

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            if (category.getCategoryId().equals(selectedCategoryId)) {
                // tapping the already-selected category again clears the filter
                selectedCategoryId = null;
                Toast.makeText(this, "Showing all categories", Toast.LENGTH_SHORT).show();
            } else {
                selectedCategoryId = category.getCategoryId();
                Toast.makeText(this, "Filtering: " + category.getName(), Toast.LENGTH_SHORT).show();
            }
            applyFilters();
        });
        rvCategories.setAdapter(categoryAdapter);

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().trim().toLowerCase(Locale.getDefault());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadServices();
        loadCategories();
    }

    private void loadServices() {
        repository.fetchAllServices(new RepairFirestoreRepository.OnServicesLoaded() {
            @Override
            public void onLoaded(List<RepairService> services) {
                allServices.clear();
                allServices.addAll(services);
                applyFilters();

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

    private void loadCategories() {
        repository.fetchAllCategories(new RepairFirestoreRepository.OnCategoriesLoaded() {
            @Override
            public void onLoaded(List<DeviceCategory> categories) {
                categoryList.clear();
                categoryList.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HomeActivity.this,
                        "Failed to load categories: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            new DatabaseHelper(this).clearUserData();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyFilters() {
        displayedServices.clear();

        for (RepairService service : allServices) {
            boolean matchesCategory = (selectedCategoryId == null)
                    || selectedCategoryId.equals(service.getCategoryId());

            boolean matchesSearch = currentSearchText.isEmpty()
                    || service.getName().toLowerCase(Locale.getDefault()).contains(currentSearchText);

            if (matchesCategory && matchesSearch) {
                displayedServices.add(service);
            }
        }

        adapter.notifyDataSetChanged();
    }
}