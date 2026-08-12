package com.example.techfix_mobile.admin.requests;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.models.RepairRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "request_id";

    private static final String[] STATUS_OPTIONS =
            {"pending", "assigned", "in_progress", "completed", "ready_for_pickup"};

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String requestId;
    private RepairRequest currentRequest;

    private TextView tvDeviceDetails, tvIssueDesc, tvCurrentStatus;
    private Spinner spinnerTechnician, spinnerStatus;

    private final List<String> technicianIds = new ArrayList<>();
    private final List<String> technicianNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        setTitle("Request Detail");

        requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);

        tvDeviceDetails = findViewById(R.id.tvDeviceDetails);
        tvIssueDesc = findViewById(R.id.tvIssueDesc);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        spinnerTechnician = findViewById(R.id.spinnerTechnician);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        Button btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, STATUS_OPTIONS);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        loadRequest();
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadRequest() {
        db.collection("repairRequests").document(requestId).get()
                .addOnSuccessListener(doc -> {
                    RepairRequest r = doc.toObject(RepairRequest.class);
                    if (r == null) return;
                    r.setRequestId(doc.getId());
                    currentRequest = r;

                    tvDeviceDetails.setText(r.getDeviceDetails() != null ? r.getDeviceDetails() : "Device N/A");
                    tvIssueDesc.setText(r.getIssueDesc() != null ? r.getIssueDesc() : "");
                    tvCurrentStatus.setText(r.getStatus() != null ? r.getStatus() : "pending");

                    int statusIndex = indexOf(STATUS_OPTIONS, r.getStatus());
                    if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex);

                    // Only load technicians for the branch this request was auto-assigned to
                    if (r.getAssignedBranchId() != null) {
                        loadTechniciansForBranch(r.getAssignedBranchId(), r.getAssignedTechnicianId());
                    } else {
                        Toast.makeText(this, "No branch assigned to this request yet", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadTechniciansForBranch(String branchId, String currentTechnicianId) {
        db.collection("technicians").whereEqualTo("branchId", branchId).get()
                .addOnSuccessListener(snapshot -> {
                    technicianIds.clear();
                    technicianNames.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        technicianIds.add(doc.getId());
                        String name = doc.getString("name");
                        Boolean available = doc.getBoolean("isAvailable");
                        technicianNames.add(name + (Boolean.TRUE.equals(available) ? " (Available)" : " (Busy)"));
                    }

                    if (technicianIds.isEmpty()) {
                        technicianNames.add("No technicians at this branch");
                    }

                    ArrayAdapter<String> techAdapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, technicianNames);
                    techAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTechnician.setAdapter(techAdapter);

                    if (currentTechnicianId != null) {
                        int idx = technicianIds.indexOf(currentTechnicianId);
                        if (idx >= 0) spinnerTechnician.setSelection(idx);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load technicians: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveChanges() {
        if (currentRequest == null) return;

        String selectedStatus = STATUS_OPTIONS[spinnerStatus.getSelectedItemPosition()];

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", selectedStatus);

        int techPos = spinnerTechnician.getSelectedItemPosition();
        if (techPos >= 0 && techPos < technicianIds.size()) {
            updates.put("assignedTechnicianId", technicianIds.get(techPos));
        }

        if ("completed".equals(selectedStatus)) {
            updates.put("completedAt", System.currentTimeMillis());
        }

        db.collection("repairRequests").document(requestId).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int indexOf(String[] arr, String value) {
        if (value == null) return -1;
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(value)) return i;
        return -1;
    }
}