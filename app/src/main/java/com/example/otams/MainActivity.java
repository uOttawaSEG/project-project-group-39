package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// MainActivity is now an invisible "dispatcher" or "router".
// It decides where the user should go when the app starts.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        // Determine which page to display depending on login state
        boolean isLoggedIn = AuthManager.isLoggedIn();
        Intent intent;

        if (isLoggedIn) {
            intent = new Intent(MainActivity.this, DashboardActivity.class);
        } else {
            intent = new Intent(MainActivity.this, WelcomeActivity.class);
        }

        startActivity(intent);
    }
}
