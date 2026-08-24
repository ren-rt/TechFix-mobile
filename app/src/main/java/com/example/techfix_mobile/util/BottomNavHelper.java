package com.example.techfix_mobile.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.techfix_mobile.BranchListActivity;
import com.example.techfix_mobile.ProfileActivity;
import com.example.techfix_mobile.R;
import com.example.techfix_mobile.ui.history.RepairHistoryActivity;
import com.example.techfix_mobile.ui.home.HomeActivity;
import com.example.techfix_mobile.ui.myrequests.MyRequestsActivity;

/**
 * Wires up the shared {@code bottom_nav.xml} include on the 5 top-level
 * customer screens (Home, My Requests, Repair History, Branches, Profile).
 * Call {@link #setup(Activity, int)} once at the end of onCreate, after
 * setContentView, on any activity whose layout includes bottom_nav.xml.
 */
public class BottomNavHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_REQUESTS = 1;
    public static final int TAB_HISTORY = 2;
    public static final int TAB_BRANCHES = 3;
    public static final int TAB_PROFILE = 4;

    private interface Destination {
        void go(Activity activity);
    }

    public static void setup(Activity activity, int activeTab) {
        bindTab(activity, activeTab, TAB_HOME,
                R.id.navHome, R.id.navHomeIcon, R.id.navHomeLabel,
                a -> a.startActivity(new Intent(a, HomeActivity.class)));

        bindTab(activity, activeTab, TAB_REQUESTS,
                R.id.navRequests, R.id.navRequestsIcon, R.id.navRequestsLabel,
                a -> a.startActivity(new Intent(a, MyRequestsActivity.class)));

        bindTab(activity, activeTab, TAB_HISTORY,
                R.id.navHistory, R.id.navHistoryIcon, R.id.navHistoryLabel,
                a -> a.startActivity(new Intent(a, RepairHistoryActivity.class)));

        bindTab(activity, activeTab, TAB_BRANCHES,
                R.id.navBranches, R.id.navBranchesIcon, R.id.navBranchesLabel,
                a -> a.startActivity(new Intent(a, BranchListActivity.class)));

        bindTab(activity, activeTab, TAB_PROFILE,
                R.id.navProfile, R.id.navProfileIcon, R.id.navProfileLabel,
                a -> a.startActivity(new Intent(a, ProfileActivity.class)));
    }

    private static void bindTab(Activity activity, int activeTab, int tab,
                                 int rowId, int iconId, int labelId, Destination destination) {
        View row = activity.findViewById(rowId);
        if (row == null) return;

        ImageView icon = row.findViewById(iconId);
        TextView label = row.findViewById(labelId);

        int color = ContextCompat.getColor(activity, tab == activeTab ? R.color.cyan : R.color.text_faint);
        icon.setColorFilter(color);
        label.setTextColor(color);

        row.setOnClickListener(v -> {
            if (tab != activeTab) destination.go(activity);
        });
    }
}
