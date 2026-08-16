package com.example.techfix_mobile.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthHelper {

    public interface OnAuthReady {
        void onReady(String uid);
        void onError(Exception e);
    }

    public static void ensureSignedIn(OnAuthReady callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser current = auth.getCurrentUser();

        if (current != null) {
            callback.onReady(current.getUid());
            return;
        }

        // TEMPORARY: anonymous auth until Person 1's real login flow exists.
        // Replace this whole method body with FirebaseAuth.getInstance().getCurrentUser().getUid()
        // once real login is in place.
        auth.signInAnonymously()
                .addOnSuccessListener(result -> callback.onReady(result.getUser().getUid()))
                .addOnFailureListener(callback::onError);
    }
}