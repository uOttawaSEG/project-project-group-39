package com.example.otams;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText firstName, lastName, email, phone, password;
    private RadioGroup radioGroup;
    private EditText program, highestLevelOfStudy, coursesToTeach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton backButton = findViewById(R.id.backButt);
        radioGroup = findViewById(R.id.radioGroup);
        program = findViewById(R.id.program);
        highestLevelOfStudy = findViewById(R.id.highestLevelOfStudy);
        coursesToTeach = findViewById(R.id.coursesToTeach);
        firstName = findViewById(R.id.editTextText);
        lastName = findViewById(R.id.editTextText2);
        email = findViewById(R.id.editTextTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);
        password = findViewById(R.id.editTextTextPassword);
        Button registrationButton = findViewById(R.id.registrationButton);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        program.setVisibility(View.GONE);
        highestLevelOfStudy.setVisibility(View.GONE);
        coursesToTeach.setVisibility(View.GONE);

        radioGroup.check(R.id.Student);
        program.setVisibility(View.VISIBLE);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if(checkedId == R.id.Student) {
                    program.setVisibility(View.VISIBLE);
                    highestLevelOfStudy.setVisibility(View.GONE);
                    coursesToTeach.setVisibility(View.GONE);
                }else if(checkedId == R.id.Tutor){
                    program.setVisibility(View.GONE);
                    highestLevelOfStudy.setVisibility(View.VISIBLE);
                    coursesToTeach.setVisibility(View.VISIBLE);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
                }
            });
        }

    private boolean checkInputs(String firstName, String lastName, String email, String phone, String password) {
        boolean ok = true;
        if (firstName.isEmpty()) {
            this.firstName.setError("First name required!");
            ok = false;
        }
        if (lastName.isEmpty()) {
            this.lastName.setError("Last name required!");
            ok = false;
        }
        if (email.isEmpty()) {
            this.email.setError("Email required!");
            ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Invalid email!");
            ok = false;
        }
        if (phone.isEmpty()) {
            this.phone.setError("Phone required!");
            ok = false;
        } else if (phone.length() != 10) {
            this.phone.setError("Phone number must be 10 digits!");
            ok = false;
        }
        if (password.isEmpty()) {
            this.password.setError("Password required!");
            ok = false;
        } else if (password.length() < 8 || !password.matches(".*[^a-zA-Z0-9 ].*") || !password.matches(".*[0-9].*")) {
            this.password.setError("Password must be at least 8 characters long and contain at least one special character and one number!");
            ok = false;
        }
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.Student) {
            if (program.getText().toString().trim().isEmpty()) {
                program.setError("Program is required!");
                ok = false;
            }
        } else if (selectedId == R.id.Tutor) {
            if (highestLevelOfStudy.getText().toString().trim().isEmpty()) {
                highestLevelOfStudy.setError("Highest level of study is required!");
                ok = false;
            }
            if (coursesToTeach.getText().toString().trim().isEmpty()) {
                coursesToTeach.setError("Courses to teach are required!");
                ok = false;
            }
        }
        return ok;
    }

    private void registerUser() {
        String userFirstName = firstName.getText().toString().trim();
        String userLastName = lastName.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (!checkInputs(userFirstName, userLastName, userEmail, userPhone, userPassword)) {
            return;
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        assert auth.getCurrentUser() != null;
                        String uid = auth.getCurrentUser().getUid();
                        String role = (radioGroup.getCheckedRadioButtonId() == R.id.Student) ? "Student" : "Tutor";

                        java.util.Map<String, Object> user = new java.util.HashMap<>();
                        user.put("firstName", userFirstName);
                        user.put("lastName", userLastName);
                        user.put("email", userEmail);
                        user.put("phone", userPhone);
                        user.put("role", role);

                        if (role.equals("Student")) {
                            user.put("program", program.getText().toString().trim());
                        } else {
                            user.put("highestLevelOfStudy", highestLevelOfStudy.getText().toString().trim());
                            user.put("coursesToTeach", coursesToTeach.getText().toString().trim());
                        }

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}
