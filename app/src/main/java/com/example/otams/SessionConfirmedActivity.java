package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class SessionConfirmedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_confirmed);

        // Handle back button
        ImageButton backButton = findViewById(R.id.backButt);

        backButton.setOnClickListener(click -> {
            Intent intent = new Intent(SessionConfirmedActivity.this, TutorDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Display user info
        Intent intent = getIntent();
        String bookedBy = intent.getStringExtra("bookedBy");
        String formattedTime = intent.getStringExtra("formattedTime");
        String sessionId = intent.getStringExtra("sessionId");

        if (bookedBy != null && sessionId != null) {
            DataManager.getUserData(SessionConfirmedActivity.this, bookedBy, new DataManager.DataCallback() {
                @Override
                public void onSuccess(DocumentSnapshot userData) {
                    TextView timeDetails = findViewById(R.id.detail_session_time);
                    TextView studentDetails = findViewById(R.id.detail_student_name);
                    TextView contactDetails = findViewById(R.id.detail_student_contact);

                    timeDetails.setText(formattedTime);
                    studentDetails.setText(userData.getString("firstName") + " " + userData.getString("lastName"));
                    contactDetails.setText(userData.getString("email") + " / " + userData.getString("phone"));

                    Button cancelButton = findViewById(R.id.cancel_session_button);

                    cancelButton.setOnClickListener(click -> {
                        DataManager.updateData(SessionConfirmedActivity.this, "slots", sessionId, new HashMap<String, Object>() {
                            {
                                put("isAvailable", true);
                                put("bookedBy", null);
                                put("isApproved", false);
                            }
                        }, new DataManager.DataCallback() {
                            @Override
                            public void onSuccess(DocumentSnapshot data) {
                                Intent intent = new Intent(SessionConfirmedActivity.this, TutorDashboardActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(SessionConfirmedActivity.this, "Could not cancel session " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(SessionConfirmedActivity.this, "Could not fetch booker data " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
