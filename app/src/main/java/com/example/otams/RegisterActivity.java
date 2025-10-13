package com.example.otams;

import android.text.style.DrawableMarginSpan;

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


public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText firstName, lastName, email, phone, password;
    private RadioGroup radioGroup;
    private Button registrationButton;
    private EditText program, highestLevelOfStudy, coursesToTeach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton backButton = findViewById(R.id.backButt);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        EditText program = findViewById(R.id.program);
        EditText highestLevelOfStudy = findViewById(R.id.highestLevelOfStudy);
        EditText coursesToTeach = findViewById(R.id.coursesToTeach);
        firstName = findViewById(R.id.editTextText);
        lastName = findViewById(R.id.editTextText2);
        email = findViewById(R.id.editTextTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);
        password = findViewById(R.id.editTextTextPassword);
        registrationButton = findViewById(R.id.registrationButton);



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
                Boolean verified = checkInputs(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString(), phone.getText().toString(), password.getText().toString());
                registerUser();
                if(verified) {
                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                    startActivity(intent);
                }else{
                    return;
                }
            }
        });


    }


    private boolean checkInputs(String firstName, String lastName, String email, String phone, String password){
        boolean ok = true;
        if(firstName.isEmpty()){
            this.firstName.setError("First name required!");
            ok = false;
        }
        if(lastName.isEmpty()) {
            this.lastName.setError("Last name required!");
            ok = false;
        }
        if(email.isEmpty()){
            this.email.setError("Email required!");
            ok = false;

        }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            this.email.setError("Invalid email!");
            ok = false;
        }
        if(phone.isEmpty()){
            this.phone.setError("Phone required!");
            ok = false;
        }else if(phone.length() != 10){
            this.phone.setError("Phone number must be 10 digits!");
            ok = false;
        }
        if(password.isEmpty()){
            this.password.setError("Password required!");
            ok = false;
        }else if(password.length() < 8 || !password.matches(".*[^a-zA-Z0-9 ].*")|| !password.matches(".*[0-9].*")){
            this.password.setError("Password must be at least 8 characters long and contain at least one special character and one number!");
        }

        return ok;
    }
    private void registerUser() {
        String userFirstName = firstName.getText().toString().trim();
        String userLastName = lastName.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if(!checkInputs(userFirstName, userLastName, userEmail, userPhone, userPassword)){
            return;
        }else{
            if(this.radioGroup.getCheckedRadioButtonId() == R.id.Student){
                String uid = this.auth.getCurrentUser().getUid();
                String role = this.radioGroup.getCheckedRadioButtonId() == R.id.Student ? "Student" : "Tutor";
                String program = this.program.getText().toString().trim();
                System.out.println(uid + role + program);
            }else {
                this.auth.createUserWithEmailAndPassword(userEmail, userPassword);
                String uid = this.auth.getCurrentUser().getUid();
                String role = this.radioGroup.getCheckedRadioButtonId() == R.id.Student ? "Student" : "Tutor";
                String program = this.program.getText().toString().trim();
                String highestLevelOfStudy = this.highestLevelOfStudy.getText().toString().trim();
                String coursesToTeach = this.coursesToTeach.getText().toString().trim();
                System.out.println(uid + role + highestLevelOfStudy + coursesToTeach);
            }
        }

    }

}
