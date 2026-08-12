package com.example.techfix_mobile.admin.requests;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.models.RepairRequest;

import java.util.ArrayList;
import java.util.List;

public class IncomingRequestsActivity extends AppCompatActivity {

    private final List<RepairRequest> items = new ArrayList<>();
    private RequestAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_requests);
        setTitle("Incoming Repair Requests");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RequestAdapter(items, request -> {
            Intent i = new Intent(this, RequestDetailActivity.class);
            i.putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, request.getRequestId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private void loadRequests() {
        FirebaseFirestore.getInstance().collection("repairRequests")
                .get()
                .addOnSuccessListener(snapshot -> {
                    items.clear();
                    snapshot.getDocuments().forEach(doc -> {
                        RepairRequest r = doc.toObject(RepairRequest.class);
                        if (r != null) {
                            r.setRequestId(doc.getId());
                            items.add(r);
                        }
                    });
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}