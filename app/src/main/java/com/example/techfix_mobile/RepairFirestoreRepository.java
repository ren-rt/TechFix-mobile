package com.example.techfix_mobile;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepairFirestoreRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnBranchesLoaded {
        void onLoaded(List<Branch> branches, Set<String> availableBranchIds);
        void onError(Exception e);
    }

    public void fetchBranchesWithAvailability(OnBranchesLoaded callback) {
        db.collection("branches").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Branch> branches = new ArrayList<>();
                Set<String> ids = new HashSet<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Branch b = doc.toObject(Branch.class);
                    // Ensure branchId is set if not in Firestore doc
                    if (b.getBranchId() == null) b.setBranchId(doc.getId());
                    branches.add(b);
                    ids.add(b.getBranchId());
                }
                callback.onLoaded(branches, ids);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}