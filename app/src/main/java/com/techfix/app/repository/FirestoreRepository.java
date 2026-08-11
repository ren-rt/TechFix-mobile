package com.techfix.app.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.admin.resources.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnItemsLoaded {
        void onLoaded(List<Map<String, Object>> items);
        void onError(Exception e);
    }

    public interface OnComplete {
        void onSuccess();
        void onError(Exception e);
    }

    public void fetchAll(ResourceType type, OnItemsLoaded callback) {
        db.collection(type.collectionName).get().addOnSuccessListener(snapshot -> {
            List<Map<String, Object>> items = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data != null) {
                    data.put(type.idFieldKey, doc.getId());
                    items.add(data);
                }
            }
            callback.onLoaded(items);
        }).addOnFailureListener(callback::onError);
    }

    public void save(ResourceType type, String id, Map<String, Object> data, OnComplete callback) {
        if (id == null) {
            db.collection(type.collectionName).document().set(data)
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            db.collection(type.collectionName).document(id).set(data)
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        }
    }

    public void delete(ResourceType type, String id, OnComplete callback) {
        db.collection(type.collectionName).document(id).delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}