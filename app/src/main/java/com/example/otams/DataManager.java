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

    public interface updateCallback{
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

    public static void getDataOfType(Activity activity, String key, Object value, QueryCallback callback) {
        // Retrieve the data where the key = value
        getDb().collection("users").whereEqualTo(key, value).get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    Toast.makeText(activity, err.getMessage(), Toast.LENGTH_LONG).show();
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

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                        String fcmToken = tokenTask.getResult();
                        data.put("fcmToken", fcmToken);

                        getDb().collection("users").document(uid).set(data)
                                .addOnSuccessListener(activity, nVoid -> {
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    callback.onFailure(null);
                                });

                    }else{
                        String error = tokenTask.getException() != null? tokenTask.getException().getMessage() : "Failed";
                        callback.onFailure(error);
                    }
                });
    }


    public static void updateData(Activity activity, String docUid, HashMap<String, Object> data, DataCallback callback) {
        getDb().collection("users").document(docUid).update(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void createAvailabilitySlot(Activity activity, HashMap<String, Object> data, DataCallback callback) {
        getDb().collection("availabilitySlots").add(data)
                .addOnSuccessListener(activity, docRef -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void getSlotsOfType(Activity activity, String key, Object value, QueryCallback callback) {
        // Retrieve the data where the key = value in the new collection
        getDb().collection("availabilitySlots").whereEqualTo(key, value).get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    // Optionally remove Toast if you want silent failure, but helpful for debugging
                    Toast.makeText(activity, err.getMessage(), Toast.LENGTH_LONG).show();
                    callback.onFailure(err.getMessage());
                });
    }

    public static void deleteAvailabilitySlot(Activity activity, String docUid, DataCallback callback) {
        getDb().collection("availabilitySlots").document(docUid).delete()
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }

    public static void createSessionRequest(Activity activity, HashMap<String, Object> data, DataCallback callback) {
        getDb().collection("sessionRequests").add(data)
                .addOnSuccessListener(activity, docRef -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void getSessionRequestsOfType(Activity activity, String key, Object value, QueryCallback callback) {
        getDb().collection("sessionRequests").whereEqualTo(key, value).get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    callback.onFailure(err.getMessage());
                });
    }

    public static void updateSessionRequest(Activity activity, String docUid, HashMap<String, Object> data, DataCallback callback) {
        getDb().collection("sessionRequests").document(docUid).update(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }



}