package com.example.otams;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Document;

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

    public interface QueryCallback {
        void onSuccess(QuerySnapshot data);
        void onFailure(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();
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
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, doc -> {
                    callback.onFailure(null);
                });
    }

    public static void getDataOfType(Activity activity, String collectionName, String key, Object value, QueryCallback callback) {
        // Retrieve the data where the key = value
        getDb().collection(collectionName).whereEqualTo(key, value).get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    Toast.makeText(activity, err.getMessage(), Toast.LENGTH_LONG).show();
                    callback.onFailure(null);
                });
    }

    public static void createData(Activity activity, String collectionName, HashMap<String, Object> data, DataCallback callback) {
        // Retrieve the current user
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("No user is logged in");

            return;
        }

        // Create the user's data
        String uid = currentUser.getUid();

        getDb().collection(collectionName).document(uid).set(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(null);
                });
    }

    public static void updateData(Activity activity, String collectionName, String docUid, HashMap<String, Object> data, DataCallback callback) {
        getDb().collection(collectionName).document(docUid).update(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void deleteData(Activity activity, String collectionName, String docUid, UpdateCallback callback) {
        getDb().collection(collectionName).document(docUid).delete()
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }
}