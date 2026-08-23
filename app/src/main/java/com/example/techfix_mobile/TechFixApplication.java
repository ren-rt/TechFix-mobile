package com.example.techfix_mobile;

import android.app.Application;
import android.preference.PreferenceManager;
import org.osmdroid.config.Configuration;

public class TechFixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Configure osmdroid globally once before any MapView loads
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        // OSM requires a unique user agent
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}