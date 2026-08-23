package com.example.techfix_mobile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobile.model.Branch;
import com.example.techfix_mobile.data.remote.RepairFirestoreRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class BranchListActivity extends AppCompatActivity {

    public static final String EXTRA_HIGHLIGHT_BRANCH_ID = "highlight_branch_id";

    private final RepairFirestoreRepository repository = new RepairFirestoreRepository();
    private final List<Branch> branches = new ArrayList<>();

    private MapView map;
    private String highlightBranchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_list);

        highlightBranchId = getIntent().getStringExtra(EXTRA_HIGHLIGHT_BRANCH_ID);

        map = findViewById(R.id.map);
        map.setTileSource(new XYTileSource(
                "CartoLight", 0, 20, 256, ".png",
                new String[]{
                        "https://a.basemaps.cartocdn.com/light_all/",
                        "https://b.basemaps.cartocdn.com/light_all/",
                        "https://c.basemaps.cartocdn.com/light_all/"
                }));
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        map.getController().setCenter(new GeoPoint(6.9271, 79.8612));

        map.getViewTreeObserver().addOnGlobalLayoutListener(
                new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        map.invalidate();
                    }
                });

        RecyclerView rvBranches = findViewById(R.id.rvBranches);
        rvBranches.setLayoutManager(new LinearLayoutManager(this));
        BranchAdapter adapter = new BranchAdapter(branches, this::focusOnBranch);
        rvBranches.setAdapter(adapter);

        loadBranches(adapter);
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

    private void loadBranches(BranchAdapter adapter) {
        repository.fetchBranchesWithAvailability(new RepairFirestoreRepository.OnBranchesLoaded() {
            @Override
            public void onLoaded(List<Branch> loaded, java.util.Set<String> availableBranchIds) {
                branches.clear();
                branches.addAll(loaded);
                adapter.notifyDataSetChanged();
                drawMarkers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BranchListActivity.this, "Couldn't load branches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawMarkers() {
        if (map == null || branches.isEmpty()) return;

        map.getOverlays().clear();
        List<GeoPoint> points = new ArrayList<>();
        Branch toHighlight = null;

        for (Branch branch : branches) {
            GeoPoint position = new GeoPoint(branch.getLat(), branch.getLng());
            Marker marker = new Marker(map);
            marker.setPosition(position);
            marker.setTitle(branch.getName());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
            points.add(position);
            if (branch.getBranchId() != null && branch.getBranchId().equals(highlightBranchId)) {
                toHighlight = branch;
            }
        }

        if (toHighlight != null) {
            map.getController().animateTo(new GeoPoint(toHighlight.getLat(), toHighlight.getLng()));
            map.getController().setZoom(15.0);
        } else if (points.size() == 1) {
            map.getController().animateTo(points.get(0));
            map.getController().setZoom(13.0);
        } else if (!points.isEmpty()) {
            map.post(() -> map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 80));
        }
        map.invalidate();
    }

    private void focusOnBranch(Branch branch) {
        if (map == null) return;
        map.getController().animateTo(new GeoPoint(branch.getLat(), branch.getLng()));
        map.getController().setZoom(15.0);
    }
}