package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton registerButton = findViewById(R.id.backButt);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        EditText program = findViewById(R.id.program);
        EditText highestLevelOfStudy = findViewById(R.id.highestLevelOfStudy);
        EditText coursesToTeach = findViewById(R.id.coursesToTeach);
        if(radioGroup.getCheckedRadioButtonId() == R.id.Student) {
            program.setVisibility(View.VISIBLE);
            highestLevelOfStudy.setVisibility(View.GONE);
            coursesToTeach.setVisibility(View.GONE);
        }else if(radioGroup.getCheckedRadioButtonId() == R.id.Tutor){
            program.setVisibility(View.GONE);
            highestLevelOfStudy.setVisibility(View.VISIBLE);
            coursesToTeach.setVisibility(View.VISIBLE);
        }
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });


    }

}
