package com.example.techfix_mobile.data.remote;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.techfix_mobile.model.RepairService;
import com.example.techfix_mobile.model.RepairRequest;
import com.example.techfix_mobile.model.Branch;
import com.example.techfix_mobile.model.DeviceCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class RepairFirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnServicesLoaded {
        void onLoaded(List<RepairService> services);
        void onError(Exception e);
    }

    public interface OnRequestCreated {
        void onSuccess(String requestId);
        void onError(Exception e);
    }

    public void fetchAllServices(OnServicesLoaded callback) {
        db.collection("repairServices").get().addOnSuccessListener(snapshot -> {
            List<RepairService> services = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                RepairService s = new RepairService(
                        doc.getId(),
                        doc.getString("categoryId"),
                        doc.getString("name"),
                        doc.getString("description"),
                        doc.getDouble("basePrice") != null ? doc.getDouble("basePrice") : 0.0,
                        doc.getLong("estTimeHrs") != null ? doc.getLong("estTimeHrs").intValue() : 0
                );
                services.add(s);
            }
            callback.onLoaded(services);
        }).addOnFailureListener(callback::onError);
    }

    public void createRepairRequest(RepairRequest request, OnRequestCreated callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("customerId", request.getCustomerId());
        data.put("serviceId", request.getServiceId());
        data.put("categoryId", request.getCategoryId());
        data.put("deviceDetails", request.getDeviceDetails());
        data.put("issueDesc", request.getIssueDesc());
        data.put("devicePhotoUrl", request.getDevicePhotoUrl());
        data.put("assignedBranchId", request.getAssignedBranchId());
        data.put("assignedTechnicianId", request.getAssignedTechnicianId());
        data.put("status", "pending");
        data.put("customerLat", request.getCustomerLat());
        data.put("customerLng", request.getCustomerLng());
        data.put("requestedAt", request.getRequestedAt());
        data.put("completedAt", null);

        db.collection("repairRequests").add(data)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onError);
    }
    public interface OnBranchesLoaded {
        void onLoaded(List<Branch> branches, Set<String> availableBranchIds);
        void onError(Exception e);
    }

    public void fetchBranchesWithAvailability(OnBranchesLoaded callback) {
        db.collection("branches").get().addOnSuccessListener(branchSnap -> {
            List<Branch> branches = new ArrayList<>();
            for (DocumentSnapshot doc : branchSnap.getDocuments()) {
                branches.add(new Branch(
                        doc.getId(),
                        doc.getString("name"),
                        doc.getString("address"),
                        doc.getDouble("lat") != null ? doc.getDouble("lat") : 0.0,
                        doc.getDouble("lng") != null ? doc.getDouble("lng") : 0.0
                ));
            }

            db.collection("technicians").whereEqualTo("isAvailable", true).get()
                    .addOnSuccessListener(techSnap -> {
                        Set<String> availableBranchIds = new HashSet<>();
                        for (DocumentSnapshot doc : techSnap.getDocuments()) {
                            String branchId = doc.getString("branchId");
                            if (branchId != null) availableBranchIds.add(branchId);
                        }
                        callback.onLoaded(branches, availableBranchIds);
                        // TODO: once Person 4 finishes spareParts (branchId + stockQty),
                        // also cross-check required part stock here before finalizing eligibility
                    })
                    .addOnFailureListener(callback::onError);
        }).addOnFailureListener(callback::onError);
    }

    public interface OnCategoriesLoaded {
        void onLoaded(List<DeviceCategory> categories);
        void onError(Exception e);
    }

    public void fetchAllCategories(OnCategoriesLoaded callback) {
        db.collection("deviceCategories").get().addOnSuccessListener(snapshot -> {
            List<DeviceCategory> categories = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                categories.add(new DeviceCategory(doc.getId(), doc.getString("name")));
            }
            callback.onLoaded(categories);
        }).addOnFailureListener(callback::onError);
    }
}