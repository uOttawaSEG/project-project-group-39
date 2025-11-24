package com.example.otams;

import android.app.Activity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

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

    public enum QueryType {
        EQUAL_TO,            // whereEqualTo(key, value)
        GREATER_THAN,        // whereGreaterThan(key, value)
        LESS_THAN,           // whereLessThan(key, value)
        ARRAY_CONTAINS,      // whereArrayContains(key, value)
        CONTAINS_STRING      // Custom: Requires start/end filters (for full-text search, look into external solutions)
    }

    public static class QueryParam {
        public final String key;
        public final Object value;
        public final QueryType type;

        public QueryParam(String key, Object value, QueryType type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }
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
                .addOnFailureListener(activity, err -> {
                    callback.onFailure(err.getMessage());
                });
    }

    public static void getDataById(Activity activity, String collectionName, String uid, DataCallback callback) {
        getDb().collection(collectionName).document(uid).get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    callback.onFailure(err.getMessage());
                });
    }

    public static void getDataOfType(Activity activity, String collectionName, List<QueryParam> queryParams, QueryCallback callback) {
        Query query = getDb().collection(collectionName);

        // Iterate through the array of query parameters
        for (QueryParam param : queryParams) {
            switch (param.type) {
                case EQUAL_TO:
                    query = query.whereEqualTo(param.key, param.value);
                    break;
                case GREATER_THAN:
                    query = query.whereGreaterThan(param.key, param.value);
                    break;
                case LESS_THAN:
                    query = query.whereLessThan(param.key, param.value);
                    break;
                case ARRAY_CONTAINS:
                    query = query.whereArrayContains(param.key, param.value);
                    break;
                case CONTAINS_STRING:
                    // Prefix search implementation (since true substring search isn't native)
                    if (param.value instanceof String) {
                        String prefix = (String) param.value;
                        query = query.whereGreaterThanOrEqualTo(param.key, prefix)
                                .whereLessThanOrEqualTo(param.key, prefix + "\uf8ff");
                    }
                    break;
            }
        }

        // Execute the final chained query
        query.get()
                .addOnSuccessListener(activity, callback::onSuccess)
                .addOnFailureListener(activity, err -> {
                    Toast.makeText(activity, err.getMessage(), Toast.LENGTH_LONG).show();
                    callback.onFailure(null);
                });
    }

    public static void createData(Activity activity, String collectionName, Boolean generateUid, HashMap<String, Object> data, DataCallback callback) {
        // Retrieve the current user
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) {
            callback.onFailure("No user is logged in");

            return;
        }

        // Create the user's data
        String uid = currentUser.getUid();

        if (generateUid) {
            getDb().collection(collectionName).add(data)
                    .addOnSuccessListener(activity, nVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e.getMessage());
                    });

            return;
        }

        getDb().collection(collectionName).document(uid).set(data)
                .addOnSuccessListener(activity, nVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
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