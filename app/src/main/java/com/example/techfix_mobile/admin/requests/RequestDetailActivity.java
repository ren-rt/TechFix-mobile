package com.example.techfix_mobile.admin.requests;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.model.RepairRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "request_id";

    private static final String[] STATUS_OPTIONS =
            {"pending", "assigned", "in_progress", "completed", "ready_for_pickup"};
    private static final int CAMERA_PERMISSION_CODE = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String requestId;
    private RepairRequest currentRequest;

    private TextView tvDeviceDetails, tvIssueDesc, tvCurrentStatus;
    private Spinner spinnerTechnician, spinnerStatus;
    private ImageView imgBefore, imgAfter;
    private TextView tvPaymentStatus;

    private final List<String> technicianIds = new ArrayList<>();
    private final List<String> technicianNames = new ArrayList<>();

    private String capturingType;
    private Uri pendingPhotoUri;
    private String pendingPhotoPath;

    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request_detail);
        setTitle("Request Detail");

        requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);

        tvDeviceDetails = findViewById(R.id.tvDeviceDetails);
        tvIssueDesc = findViewById(R.id.tvIssueDesc);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        spinnerTechnician = findViewById(R.id.spinnerTechnician);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        imgBefore = findViewById(R.id.imgBefore);
        imgAfter = findViewById(R.id.imgAfter);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCaptureBefore = findViewById(R.id.btnCaptureBefore);
        Button btnCaptureAfter = findViewById(R.id.btnCaptureAfter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, STATUS_OPTIONS);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && pendingPhotoPath != null) {
                handleCapturedPhoto(capturingType, pendingPhotoPath);
            } else {
                Toast.makeText(this, "Capture cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        btnCaptureBefore.setOnClickListener(v -> startCapture("before"));
        btnCaptureAfter.setOnClickListener(v -> startCapture("after"));

        loadRequest();
        loadPaymentStatus();
        loadExistingPhotos();
        btnSave.setOnClickListener(v -> saveChanges());
    }

    // ================= PAYMENT STATUS (read-only, Person 3's data) =================

    private void loadPaymentStatus() {
        db.collection("payments")
                .whereEqualTo("requestId", requestId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        tvPaymentStatus.setText("Not paid yet");
                        return;
                    }
                    String status = snapshot.getDocuments().get(0).getString("status");
                    tvPaymentStatus.setText(status != null ? status : "unknown");
                })
                .addOnFailureListener(e -> tvPaymentStatus.setText("Unable to load"));
    }

    // ================= PHOTO CAPTURE =================

    private void startCapture(String type) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }
        capturingType = type;
        try {
            File photoFile = createTempImageFile();
            pendingPhotoPath = photoFile.getAbsolutePath();
            pendingPhotoUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(pendingPhotoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Could not create photo file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File cacheDir = getCacheDir();
        return File.createTempFile("PHOTO_" + timeStamp, ".jpg", cacheDir);
    }

    private void handleCapturedPhoto(String type, String filePath) {
        Bitmap original = BitmapFactory.decodeFile(filePath);
        if (original == null) {
            Toast.makeText(this, "Failed to read captured photo", Toast.LENGTH_SHORT).show();
            return;
        }

        int width = original.getWidth();
        int height = original.getHeight();
        int newWidth, newHeight;
        if (width >= height) {
            newWidth = 800;
            newHeight = (int) (800.0 * height / width);
        } else {
            newHeight = 800;
            newWidth = (int) (800.0 * width / height);
        }
        Bitmap resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);

        if ("before".equals(type)) {
            imgBefore.setImageBitmap(resized);
        } else {
            imgAfter.setImageBitmap(resized);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        uploadPhoto(type, base64);
    }

    private void uploadPhoto(String type, String base64Data) {
        Map<String, Object> photo = new HashMap<>();
        photo.put("requestId", requestId);
        photo.put("type", type);
        photo.put("data", base64Data);
        photo.put("uploadedAt", FieldValue.serverTimestamp());

        db.collection("repairPhotos").add(photo)
                .addOnSuccessListener(docRef ->
                        Toast.makeText(this, type + " photo saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadExistingPhotos() {
        loadPhotoOfType("before", imgBefore);
        loadPhotoOfType("after", imgAfter);
    }

    private void loadPhotoOfType(String type, ImageView target) {
        db.collection("repairPhotos")
                .whereEqualTo("requestId", requestId)
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) return;
                    String base64 = snapshot.getDocuments().get(0).getString("data");
                    if (base64 != null) {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        target.setImageBitmap(bmp);
                    }
                });
    }

    // ================= STATUS / TECHNICIAN =================

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