package com.example.otams;

import android.app.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    // Default Vars
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Auth Result Callback Interface
    public interface AuthResultCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    // Main Methods
    public static void login(String email, String password, Activity activity, AuthResultCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public static void register(String email, String password, Activity activity, AuthResultCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public static void logout() {
        auth.signOut();
    }

    // Helper Methods
    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}