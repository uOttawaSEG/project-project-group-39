package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ImageButton backButton = findViewById(R.id.backButt);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        emailText = findViewById(R.id.emailID);
        passwordText = findViewById(R.id.passwordID);
        Button loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> {
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString();
            if (!checkInputs(email, password))
                return;

            AuthManager.login(email, password, LoginActivity.this, new AuthManager.AuthResultCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    DataManager.getData(LoginActivity.this, new DataManager.DataCallback() {
                        @Override
                        public void onSuccess(DocumentSnapshot data) {
                            String role = data.getString("role");

                            goToWelcome(role != null ? role : "Student");
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(LoginActivity.this, "Login failed (id 2): " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, "Wrong email or password", Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    private boolean checkInputs(String email, String password) {
        boolean ok = true;
        if (email.isEmpty()) {
            emailText.setError("Email required!");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Sorry! This is an invalid email.");
            ok = false;
        }
        if (password.isEmpty()) {
            passwordText.setError("Password required!");
            ok = false;
        }
        return ok;
    }

    private void goToWelcome(String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
        finish();
    }
}