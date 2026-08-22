package com.example.techfix_mobile.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.CurrentLocationRequest;

public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface OnLocationResult {
        void onLocationFound(double lat, double lng);
        void onLocationError(String message);
    }

    public static boolean hasLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static void getCurrentLocation(Activity activity, OnLocationResult callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        FusedLocationProviderClient fusedClient =
                LocationServices.getFusedLocationProviderClient(activity);

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        try {
            fusedClient.getCurrentLocation(request, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationFound(location.getLatitude(), location.getLongitude());
                        } else {
                            callback.onLocationError("Unable to get current location — try again outdoors or check GPS is on");
                        }
                    })
                    .addOnFailureListener(e -> callback.onLocationError(e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Location permission was revoked unexpectedly");
        }
    }
}