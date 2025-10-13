package com.example.otams;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class DataManager {
    // Default Vars
    private static FirebaseFirestore getDb() {
        return FirebaseFirestore.getInstance();
    }

    public interface DataCallback {
        void onSuccess(DocumentSnapshot data);
        void onFailure(String errorMessage);
    }

    // Main Methods
    public static void getData(Activity activity, DataCallback callback) {
        // Retrieve the current user
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("No user is logged in");

            return;
        }

        // Now send their data
        String uid = currentUser.getUid();

        getDb().collection("users").document(uid).get()
                .addOnSuccessListener(activity, doc -> {
                    callback.onSuccess(doc);
                })
                .addOnFailureListener(activity, doc -> {
                    callback.onFailure(null);
                });
    }

    public static void createData(Activity activity, HashMap<String, Object> data, DataCallback callback) {
        // Retrieve the current user
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("No user is logged in");

            return;
        }

        // Create the user's data
        String uid = currentUser.getUid();

        getDb().collection("users").document(uid).set(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(null);
                });
    }
}