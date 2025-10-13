package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

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
            DataManager.getData(MainActivity.this, new DataManager.DataCallback() {
                @Override
                public void onSuccess(DocumentSnapshot data) {

                    Intent intent;

                    String role = data.getString("role");

                    if (Objects.equals(role, "Tutor")) {
                        intent = new Intent(MainActivity.this, TutorDashboardActivity.class);
                    } else if (Objects.equals(role, "Student")) {
                        intent = new Intent(MainActivity.this, StudentDashboardActivity.class);
                    } else if (Objects.equals(role, "Admin")) {
                        intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    }

                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            intent = new Intent(MainActivity.this, WelcomeActivity.class);

            startActivity(intent);
        }
    }
}
