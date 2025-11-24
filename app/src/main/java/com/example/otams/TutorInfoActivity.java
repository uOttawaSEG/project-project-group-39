package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class TutorInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutor_availability_slots);

        // Handle back button
        ImageButton backButton = findViewById(R.id.backButt);

        backButton.setOnClickListener(click -> {
            Intent intent = new Intent(TutorInfoActivity.this, StudentDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Default Vars
        String tutorId = getIntent().getStringExtra("tutorId");

        // Fetching Data
        DataManager.getDataById(TutorInfoActivity.this, "users", tutorId, new DataManager.DataCallback() {
            @Override
            public void onSuccess(DocumentSnapshot data) {
                // Retrieve tutor data
                String tutorName = data.getString("firstName") + " " + data.getString("lastName");
                String tutorEmail = data.getString("email");
                String tutorPhoneNumber = data.getString("phone");
                Double totalRating = data.getDouble("totalRating");
                Double numRatings = data.getDouble("numRatings");
                Number avgRating = totalRating != null && numRatings != null ? totalRating / numRatings : 0;

                TextView detailTutorName = findViewById(R.id.detail_tutor_name);
                TextView detailTutorEmail = findViewById(R.id.detail_tutor_email);
                TextView detailTutorPhoneNumber = findViewById(R.id.detail_tutor_phonenum);
                TextView detailTutorRating = findViewById(R.id.detail_tutor_rating);

                // Set tutor data
                detailTutorName.setText(tutorName);
                detailTutorEmail.setText(tutorEmail);
                detailTutorPhoneNumber.setText(tutorPhoneNumber);
                detailTutorRating.setText(avgRating.toString() + " Stars");

                // Retrieve slots
                DataManager.getDataOfType(TutorInfoActivity.this, "slots", new ArrayList<>() {{
                    add(new DataManager.QueryParam("tutorId", tutorId, DataManager.QueryType.EQUAL_TO));
                    add(new DataManager.QueryParam("isAvailable", true, DataManager.QueryType.EQUAL_TO));
                }}, new DataManager.QueryCallback() {
                    @Override
                    public void onSuccess(QuerySnapshot data) {
                        // Default Vars
                        LinearLayout currentList = findViewById(R.id.timeslotsListContainer);
                        LayoutInflater inflater = LayoutInflater.from(TutorInfoActivity.this);

                        // Clear any old templates
                        currentList.removeAllViews();

                        // Add in all the templates
                        for (DocumentSnapshot document : data) {
                            // Make sure the timeslot is not a previous session
                            Timestamp startTime = document.getTimestamp("startTime");

                            if (startTime == null || startTime.compareTo(Timestamp.now()) < 0) {
                                continue;
                            }

                            View timeslot = inflater.inflate(R.layout.tutor_timeslot, currentList, false);
                            TextView timeslotDetails = timeslot.findViewById(R.id.timeslotDetails);
                            TextView requiresApprovalText = timeslot.findViewById(R.id.requiresApproval);
                            Button bookBtn = timeslot.findViewById(R.id.book);

                            Timestamp endTime = document.getTimestamp("endTime");
                            Boolean requiresApproval = document.getBoolean("requiresApproval");

                            timeslotDetails.setText(TutorDashboardActivity.formatSlotTime(startTime, endTime));
                            requiresApprovalText.setText(String.format("Approval: %s", Boolean.TRUE.equals(requiresApproval) ? "Manual" : "Auto"));

                            bookBtn.setOnClickListener(v -> {
                                DataManager.getDataById(TutorInfoActivity.this, "slots", document.getId(), new DataManager.DataCallback() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot slotData) {
                                        FirebaseUser currentUser = AuthManager.getCurrentUser();

                                        if (currentUser == null || !Boolean.TRUE.equals(slotData.getBoolean("isAvailable"))) {
                                            return;
                                        }

                                        DataManager.updateData(TutorInfoActivity.this, "slots", document.getId(), new HashMap<>() {{
                                            put("isAvailable", false);
                                            put("bookedBy", currentUser.getUid());
                                            put("isDenied", false);

                                            if (Boolean.FALSE.equals(requiresApproval)) {
                                                put("isApproved", true);
                                                put("isPending", false);
                                            } else {
                                                put("isPending", true);
                                            }
                                        }}, new DataManager.DataCallback() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot data) {
                                                Toast.makeText(TutorInfoActivity.this, "Successfully booked slot", Toast.LENGTH_LONG).show();

                                                currentList.removeView(timeslot);
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                Toast.makeText(TutorInfoActivity.this, "Failed to book slot: " + errorMessage, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Toast.makeText(TutorInfoActivity.this, "Failed to retrieve slot: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            });

                            currentList.addView(timeslot);
                        }

                        if (data.isEmpty()) {
                            Toast.makeText(TutorInfoActivity.this, "No timeslots for this tutor", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(TutorInfoActivity.this, "Could not load slots: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorInfoActivity.this, "Could not retrieve tutor's data: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
