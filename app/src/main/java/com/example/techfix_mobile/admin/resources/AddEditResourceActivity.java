package com.example.techfix_mobile.admin.resources;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.model.Branch;
import com.example.techfix_mobile.repository.FirestoreRepository;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditResourceActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "resource_type";
    public static final String EXTRA_ID = "resource_id";

    private ResourceType type;
    private String editingId;
    private final FirestoreRepository repo = new FirestoreRepository();

    // ---- Branch-specific (map) path ----
    private EditText etName, etAddress, etContact;
    private TextView tvLatLng;
    private MapView map;
    private Marker branchMarker;
    private Double selectedLat, selectedLng;

    // ---- Generic dynamic-field path ----
    private final List<GenericFieldViews.FieldViewHolder> fieldHolders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = ResourceType.valueOf(getIntent().getStringExtra(EXTRA_TYPE));
        editingId = getIntent().getStringExtra(EXTRA_ID);
        setTitle((editingId == null ? "Add " : "Edit ") + type.displayName);

        boolean hasLatLng = false;
        for (ResourceField f : type.fields) if (f.type == FieldType.LATLNG) hasLatLng = true;

        if (hasLatLng) {
            setupBranchMapForm();
        } else {
            setupGenericForm();
        }
    }

    // ================= BRANCH (map) FORM =================

    private void setupBranchMapForm() {
        setContentView(R.layout.activity_add_edit_branch);

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etContact = findViewById(R.id.etContact);
        tvLatLng = findViewById(R.id.tvLatLng);
        Button btnSave = findViewById(R.id.btnSave);

        map = findViewById(R.id.map);
        map.setTileSource(new XYTileSource(
                "CartoLight", 0, 19, 256, ".png",
                new String[]{
                        "https://a.basemaps.cartocdn.com/light_all/",
                        "https://b.basemaps.cartocdn.com/light_all/",
                        "https://c.basemaps.cartocdn.com/light_all/"
                }));
        map.setMultiTouchControls(true);
        GeoPoint defaultPoint = new GeoPoint(6.9271, 79.8612);
        map.getController().setZoom(10.0);
        map.getController().setCenter(defaultPoint);

        org.osmdroid.events.MapEventsReceiver receiver = new org.osmdroid.events.MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedLat = p.getLatitude();
                selectedLng = p.getLongitude();
                if (branchMarker != null) map.getOverlays().remove(branchMarker);
                branchMarker = new Marker(map);
                branchMarker.setPosition(p);
                branchMarker.setTitle("Branch Location");
                map.getOverlays().add(branchMarker);
                tvLatLng.setText(String.format("Lat: %.6f, Lng: %.6f", p.getLatitude(), p.getLongitude()));
                map.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(receiver));

        if (editingId != null) loadExistingBranch();

        btnSave.setOnClickListener(v -> saveBranch());
    }

    private void loadExistingBranch() {
        FirebaseFirestore.getInstance().collection("branches").document(editingId)
                .get()
                .addOnSuccessListener(doc -> {
                    Branch b = doc.toObject(Branch.class);
                    if (b == null) return;
                    etName.setText(b.getName());
                    etAddress.setText(b.getAddress());
                    etContact.setText(b.getContactNumber());
                    selectedLat = b.getLat();
                    selectedLng = b.getLng();
                    tvLatLng.setText(String.format("Lat: %.6f, Lng: %.6f", b.getLat(), b.getLng()));
                    GeoPoint point = new GeoPoint(b.getLat(), b.getLng());
                    if (branchMarker != null) map.getOverlays().remove(branchMarker);
                    branchMarker = new Marker(map);
                    branchMarker.setPosition(point);
                    branchMarker.setTitle(b.getName());
                    map.getOverlays().add(branchMarker);
                    map.getController().setCenter(point);
                    map.getController().setZoom(14.0);
                    map.invalidate();
                });
    }

    private void saveBranch() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Name and address are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Please tap the map to set a location", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("address", address);
        data.put("contactNumber", contact);
        data.put("lat", selectedLat);
        data.put("lng", selectedLng);

        repo.save(type, editingId, data, new FirestoreRepository.OnComplete() {
            @Override public void onSuccess() {
                Toast.makeText(AddEditResourceActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(AddEditResourceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    // ================= GENERIC (Technician, and future types) FORM =================

    private void setupGenericForm() {
        setContentView(R.layout.activity_add_edit_generic);
        LinearLayout container = findViewById(R.id.fieldContainer);

        for (ResourceField field : type.fields) {
            container.addView(GenericFieldViews.createLabel(this, field.label));
            android.view.View input = GenericFieldViews.createInput(this, field);
            GenericFieldViews.FieldViewHolder holder = new GenericFieldViews.FieldViewHolder(field, input);
            fieldHolders.add(holder);
            container.addView(input);

            if (field.type == FieldType.BRANCH_DROPDOWN) {
                repo.fetchAll(ResourceType.BRANCH, new FirestoreRepository.OnItemsLoaded() {
                    @Override public void onLoaded(List<Map<String, Object>> items) {
                        GenericFieldViews.bindDropdown(AddEditResourceActivity.this, holder, items, "name", "branchId");
                        if (editingId != null) loadExistingGenericItem(); // re-apply saved value once dropdown is populated
                    }
                    @Override public void onError(Exception e) { }
                });
            } else if (field.type == FieldType.CATEGORY_DROPDOWN) {
                repo.fetchAll(ResourceType.CATEGORY, new FirestoreRepository.OnItemsLoaded() {
                    @Override public void onLoaded(List<Map<String, Object>> items) {
                        GenericFieldViews.bindDropdown(AddEditResourceActivity.this, holder, items, "name", "categoryId");
                        if (editingId != null) loadExistingGenericItem();
                    }
                    @Override public void onError(Exception e) { }
                });
            }
        }

        Button btnSave = new Button(this);
        btnSave.setText("Save " + type.displayName);
        btnSave.setBackgroundColor(0xFFEEEE30);
        btnSave.setTextColor(0xFF080800);
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        saveParams.topMargin = 48;
        btnSave.setLayoutParams(saveParams);
        btnSave.setGravity(Gravity.CENTER);
        container.addView(btnSave);

        if (editingId != null) loadExistingGenericItem();

        btnSave.setOnClickListener(v -> saveGenericItem());
    }

    private void loadExistingGenericItem() {
        FirebaseFirestore.getInstance().collection(type.collectionName).document(editingId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> data = doc.getData();
                    if (data == null) return;
                    for (GenericFieldViews.FieldViewHolder holder : fieldHolders) {
                        holder.setValue(data.get(holder.field.key));
                    }
                });
    }

    private void saveGenericItem() {
        Map<String, Object> data = new HashMap<>();
        for (GenericFieldViews.FieldViewHolder holder : fieldHolders) {
            Object value = holder.readValue();
            if ((holder.field.type == FieldType.BRANCH_DROPDOWN || holder.field.type == FieldType.CATEGORY_DROPDOWN)
                    && value == null) {
                Toast.makeText(this, holder.field.label + " is required", Toast.LENGTH_SHORT).show();
                return;
            }
            data.put(holder.field.key, value);
        }

        repo.save(type, editingId, data, new FirestoreRepository.OnComplete() {
            @Override public void onSuccess() {
                Toast.makeText(AddEditResourceActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(AddEditResourceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}