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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.HashMap;
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

        AuthManager.register(userEmail, userPassword, RegisterActivity.this, new AuthManager.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Compile the user's data
                HashMap<String, Object> data = new HashMap<>();
                String role = (radioGroup.getCheckedRadioButtonId() == R.id.Student) ? "Student" : "Tutor";

                data.put("firstName", userFirstName);
                data.put("lastName", userLastName);
                data.put("email", userEmail);
                data.put("phone", userPhone);
                data.put("password", userPassword);
                data.put("role", role);
                data.put("isPending", true);

                if (role.equals("Student")) {
                    data.put("program", program.getText().toString().trim());
                } else {
                    data.put("highestLevelOfStudy", highestLevelOfStudy.getText().toString().trim());
                    data.put("coursesToTeach", coursesToTeach.getText().toString().trim());
                }

                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                String fcmToken = tokenTask.getResult();
                                data.put("fcmToken", fcmToken);
                            }

                            // Create it in the database (regardless of FCM output)
                            DataManager.createData(RegisterActivity.this, "users", data, new DataManager.DataCallback() {
                                @Override
                                public void onSuccess(DocumentSnapshot data) {
                                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(RegisterActivity.this, "Please wait for your request to be reviewed by an administrator.", Toast.LENGTH_LONG).show();
                                    AuthManager.logout();
                                    Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(RegisterActivity.this, "Error saving user data: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
