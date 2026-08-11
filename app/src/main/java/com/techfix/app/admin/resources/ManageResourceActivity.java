package com.techfix.app.admin.resources;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.techfix.app.R;
import com.techfix.app.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageResourceActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "resource_type";

    private ResourceType type;
    private final FirestoreRepository repo = new FirestoreRepository();
    private ResourceAdapter adapter;
    private final List<Map<String, Object>> items = new ArrayList<>();
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_resource);

        type = ResourceType.valueOf(getIntent().getStringExtra(EXTRA_TYPE));
        setTitle("Manage " + type.displayName + "s");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ResourceAdapter(items, type, new ResourceAdapter.Listener() {
            @Override
            public void onEdit(Map<String, Object> item) {
                Intent i = new Intent(ManageResourceActivity.this, AddEditResourceActivity.class);
                i.putExtra(AddEditResourceActivity.EXTRA_TYPE, type.name());
                i.putExtra(AddEditResourceActivity.EXTRA_ID, String.valueOf(item.get(type.idFieldKey)));
                startActivity(i);
            }

            @Override
            public void onDelete(Map<String, Object> item) {
                new AlertDialog.Builder(ManageResourceActivity.this)
                        .setTitle("Delete " + type.displayName + "?")
                        .setMessage("This cannot be undone.")
                        .setPositiveButton("Delete", (d, w) -> {
                            String id = String.valueOf(item.get(type.idFieldKey));
                            repo.delete(type, id, new FirestoreRepository.OnComplete() {
                                @Override public void onSuccess() { loadItems(); }
                                @Override public void onError(Exception e) { }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditResourceActivity.class);
            i.putExtra(AddEditResourceActivity.EXTRA_TYPE, type.name());
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        repo.fetchAll(type, new FirestoreRepository.OnItemsLoaded() {
            @Override
            public void onLoaded(List<Map<String, Object>> loaded) {
                items.clear();
                items.addAll(loaded);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Exception e) { }
        });
    }
}
