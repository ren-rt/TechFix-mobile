package com.example.techfix_mobile.utils;

import android.location.Location;
import com.example.techfix_mobile.model.Branch;

import java.util.List;
import java.util.Set;

public class NearestBranchResolver {

    public static Branch findNearest(double customerLat, double customerLng,
                                     List<Branch> branches, Set<String> eligibleBranchIds) {
        Branch nearest = null;
        float shortestDistance = Float.MAX_VALUE;

        Location customerLoc = new Location("customer");
        customerLoc.setLatitude(customerLat);
        customerLoc.setLongitude(customerLng);

        for (Branch branch : branches) {
            if (!eligibleBranchIds.contains(branch.getBranchId())) continue; // skip branches with no available technician

            Location branchLoc = new Location("branch");
            branchLoc.setLatitude(branch.getLat());
            branchLoc.setLongitude(branch.getLng());

            float distance = customerLoc.distanceTo(branchLoc);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearest = branch;
            }
        }
        return nearest; // null if no eligible branch found
    }
}