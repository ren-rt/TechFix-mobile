package com.example.techfix_mobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import java.util.List;

public class BranchMapActivity extends AppCompatActivity {

    private MapView map = null;
    private DatabaseHelper dbHelper;

    // CARTO tiles - more reliable for coursework/labs
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

        // Configuration is handled globally in TechFixApplication

        setContentView(R.layout.activity_branch_map_osm);

        dbHelper = new DatabaseHelper(this);
        map = findViewById(R.id.map);
        map.setTileSource(cartoLight); // Use CARTO tiles
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);

        // Fix: Force redraw once the view has its final size
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
        List<Branch> branches = getBranchesFromLocal();
        if (!branches.isEmpty()) {
            GeoPoint startPoint = null;
            for (Branch b : branches) {
                GeoPoint point = new GeoPoint(b.getLat(), b.getLng());
                Marker startMarker = new Marker(map);
                startMarker.setPosition(point);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle(b.getName());
                startMarker.setSnippet(b.getAddress());
                map.getOverlays().add(startMarker);

                if (startPoint == null) startPoint = point;
            }
            if (startPoint != null) {
                map.getController().setCenter(startPoint);
            }
        }
        map.invalidate();
    }

    private List<Branch> getBranchesFromLocal() {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase sqlDb = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDb.query("branches", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("branch_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));

                String contact = "";
                try {
                    contact = cursor.getString(cursor.getColumnIndexOrThrow("contact_number"));
                } catch (Exception e) {
                    contact = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                }

                // Using the updated constructor
                list.add(new Branch(id, name, address, lat, lng, contact, contact));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
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