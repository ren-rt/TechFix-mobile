package com.example.techfix_mobile.ui.submitrequest;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.techfix_mobile.R;
import com.example.techfix_mobile.data.remote.RepairFirestoreRepository;
import com.example.techfix_mobile.model.Branch;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.utils.AuthHelper;
import com.example.techfix_mobile.utils.LocationHelper;
import com.example.techfix_mobile.utils.NearestBranchResolver;
import com.example.techfix_mobile.ui.confirmation.ConfirmationActivity;
import com.example.techfix_mobile.utils.PhotoStorageHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SubmitRequestActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "service_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";

    private String serviceId, categoryId;
    private Uri photoUri;

    private ImageView ivPhoto;
    private EditText etDeviceDetails, etIssueDesc;
    private ProgressBar progressBar;

    private final RepairFirestoreRepository repository = new RepairFirestoreRepository();

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    ivPhoto.setImageURI(photoUri);
                } else {
                    Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to capture device photo",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_request);

        serviceId = getIntent().getStringExtra(EXTRA_SERVICE_ID);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);

        ivPhoto = findViewById(R.id.ivDevicePhoto);
        etDeviceDetails = findViewById(R.id.etDeviceDetails);
        etIssueDesc = findViewById(R.id.etIssueDesc);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnCapturePhoto).setOnClickListener(v -> onCapturePhotoClicked());
        findViewById(R.id.btnFindBranch).setOnClickListener(v -> onFindBranchClicked());
    }

    private void onCapturePhotoClicked() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create photo file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile("REPAIR_" + timeStamp, ".jpg", storageDir);
    }

    private void onFindBranchClicked() {
        String deviceDetails = etDeviceDetails.getText().toString().trim();
        String issueDesc = etIssueDesc.getText().toString().trim();

        if (photoUri == null) {
            Toast.makeText(this, "Please capture a device photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (deviceDetails.isEmpty() || issueDesc.isEmpty()) {
            Toast.makeText(this, "Please fill in device details and issue description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!LocationHelper.hasLocationPermission(this)) {
            LocationHelper.requestLocationPermission(this);
            return;
        }

        setLoading(true);

        LocationHelper.getCurrentLocation(this, new LocationHelper.OnLocationResult() {
            @Override
            public void onLocationFound(double lat, double lng) {
                repository.fetchBranchesWithAvailability(new RepairFirestoreRepository.OnBranchesLoaded() {
                    @Override
                    public void onLoaded(List<Branch> branches, Set<String> availableBranchIds) {
                        Branch nearest = NearestBranchResolver.findNearest(lat, lng, branches, availableBranchIds);

                        if (nearest == null) {
                            setLoading(false);
                            Toast.makeText(SubmitRequestActivity.this,
                                    "No branch currently available. Please try again later.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        createRequest(lat, lng, nearest, deviceDetails, issueDesc);
                    }

                    @Override
                    public void onError(Exception e) {
                        setLoading(false);
                        Toast.makeText(SubmitRequestActivity.this,
                                "Failed to load branches: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onLocationError(String message) {
                setLoading(false);
                Toast.makeText(SubmitRequestActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createRequest(double lat, double lng, Branch branch,
                               String deviceDetails, String issueDesc) {
        String photoBase64;
        try {
            photoBase64 = PhotoStorageHelper.encodePhotoAsBase64(this, photoUri);
        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Failed to process photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        AuthHelper.ensureSignedIn(new AuthHelper.OnAuthReady() {
            @Override
            public void onReady(String uid) {
                RepairRequest request = new RepairRequest(
                        null,
                        uid,
                        serviceId,
                        categoryId,
                        deviceDetails,
                        issueDesc,
                        null,           // photoLocalPath — local-only, not sent to Firestore
                        photoBase64,    // devicePhotoUrl holds the base64 data URI for now
                        branch.getBranchId(),
                        null,
                        "pending",
                        lat, lng,
                        System.currentTimeMillis(),
                        0,
                        null
                );

                repository.createRepairRequest(request, new RepairFirestoreRepository.OnRequestCreated() {
                    @Override
                    public void onSuccess(String requestId) {
                        setLoading(false);
                        Intent intent = new Intent(SubmitRequestActivity.this, ConfirmationActivity.class);
                        intent.putExtra(ConfirmationActivity.EXTRA_REQUEST_ID, requestId);
                        intent.putExtra(ConfirmationActivity.EXTRA_BRANCH_NAME, branch.getName());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        setLoading(false);
                        Toast.makeText(SubmitRequestActivity.this,
                                "Failed to submit request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(SubmitRequestActivity.this,
                        "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onFindBranchClicked();
            } else {
                Toast.makeText(this, "Location permission is required to find the nearest branch",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnFindBranch).setEnabled(!loading);
    }
}