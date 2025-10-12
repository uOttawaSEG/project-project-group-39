package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailText, passwordText;
    private Button loginButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailID);
        passwordText = findViewById(R.id.passwordID);
        loginButton = findViewById(R.id.buttonLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString();
            if (!checkInputs(email, password))
                return;
            firebaseSignIn(email, password); });
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
        } else if (password.length() < 6) {
            passwordText.setError("Password too short!");
            ok = false;
        }
        return ok;
    }

    private void firebaseSignIn(String email, String password) {
        auth.signInWithemailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    String role = doc.getString("role");
                                    if (role == null) role = "Student";
                                    goToWelcome(role); })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error getting role: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                ); } 
                    else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show(); } });
    }

    private void goToWelcome(String role) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
        finish();
    }
}