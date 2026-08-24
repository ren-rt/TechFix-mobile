package com.example.techfix_mobile;

import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.techfix_mobile.data.remote.RepairFirestoreRepository;
import com.example.techfix_mobile.model.Branch;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.List;
import java.util.Set;

public class BranchMapActivity extends AppCompatActivity {

    private MapView map = null;
    private final RepairFirestoreRepository repository = new RepairFirestoreRepository();

    private final XYTileSource cartoLight = new XYTileSource(
            "CartoLight", 0, 19, 256, ".png",
            new String[]{
                    "https://a.basemaps.cartocdn.com/light_all/",
                    "https://b.basemaps.cartocdn.com/light_all/",
                    "https://c.basemaps.cartocdn.com/light_all/"
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_map_osm);

        map = findViewById(R.id.map);
        map.setTileSource(cartoLight);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);

        map.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                map.invalidate();
            }
        });

        loadMarkers();
    }

    private void loadMarkers() {
        // Branches now come straight from Firestore (same source every other
        // screen uses) instead of the local SQLite "branches" table, which
        // nothing in the app ever wrote to.
        repository.fetchBranchesWithAvailability(new RepairFirestoreRepository.OnBranchesLoaded() {
            @Override
            public void onLoaded(List<Branch> branches, Set<String> availableBranchIds) {
                renderMarkers(branches);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BranchMapActivity.this,
                        "Failed to load branches: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderMarkers(List<Branch> branches) {
        if (!branches.isEmpty()) {
            GeoPoint startPoint = null;
            for (Branch b : branches) {
                GeoPoint point = new GeoPoint(b.getLat(), b.getLng());
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(b.getName());
                marker.setSnippet(b.getAddress());
                map.getOverlays().add(marker);

                if (startPoint == null) startPoint = point;
            }
            if (startPoint != null) {
                map.getController().setCenter(startPoint);
            }
        } else {
            Toast.makeText(this, "No branches found", Toast.LENGTH_SHORT).show();
        }
        map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
