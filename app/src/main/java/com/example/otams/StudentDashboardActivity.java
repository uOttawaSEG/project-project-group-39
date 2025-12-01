package com.example.otams;

import android.app.Dialog;
import android.content.Intent;
import android.media.session.MediaController;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentdashboard);

        // Default Vars
        SearchView courseSearch = findViewById(R.id.courseSearch);
        TabLayout listSelection = findViewById(R.id.listSelection);

        // Search Handling
        courseSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateBookNewList();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        // Update Function
        listSelection.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateCurrentSelection(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        listSelection.post(() -> {
            TabLayout.Tab initialTab = listSelection.getTabAt(listSelection.getSelectedTabPosition());

            if (initialTab != null) {
                updateCurrentSelection(initialTab);
            }
        });

        // Logout
        Button logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> {
            AuthManager.logout();
            Intent intent = new Intent(StudentDashboardActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    protected void showRateDialog(String id) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_popup);

        Button closeButton = dialog.findViewById(R.id.closeButton);
        Button submitButton = dialog.findViewById(R.id.submitButton);
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);

        DataManager.getDataById(StudentDashboardActivity.this, "users", id, new DataManager.DataCallback() {
            @Override
            public void onSuccess(DocumentSnapshot data) {
                @SuppressWarnings("unchecked")
                List<String> usersVoted = (List<String>) data.get("usersVoted");
                Double totalRating = data.getDouble("totalRating");
                Double numRatings = data.getDouble("numRatings");
                Number avgRating = totalRating != null && numRatings != null ? totalRating / numRatings : 0;

                ratingBar.setRating(avgRating.floatValue());

                closeButton.setOnClickListener(v -> dialog.dismiss());

                submitButton.setOnClickListener(v -> {
                    float ratingValue = ((RatingBar) ratingBar).getRating();

                    DataManager.updateData(StudentDashboardActivity.this, "users", id, new HashMap<>() {{
                        put("totalRating", totalRating != null ? totalRating + ratingValue : ratingValue);
                        put("numRatings", numRatings != null ? numRatings + 1 : 1);

                        if (usersVoted != null) {
                            usersVoted.add(AuthManager.getCurrentUser().getUid());

                            put("usersVoted", usersVoted);
                        } else {
                            put("usersVoted", List.of(AuthManager.getCurrentUser().getUid()));
                        }
                    }}, new DataManager.DataCallback() {
                        @Override
                        public void onSuccess(DocumentSnapshot data) {
                            dialog.dismiss();
                            Toast.makeText(StudentDashboardActivity.this, "Successfully Submitted Rating", Toast.LENGTH_SHORT).show();

                            updatePreviousList();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(StudentDashboardActivity.this, "Failed to Submit Rating: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                dialog.show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void updateCurrentSelection(TabLayout.Tab selectedTab) {
        ScrollView bookNewList = findViewById(R.id.bookNewList);
        ScrollView upcomingList = findViewById(R.id.upcomingList);
        ScrollView previousList = findViewById(R.id.previousList);
        SearchView courseSearch = findViewById(R.id.courseSearch);

        if (selectedTab.getPosition() == 0) {
            // Update visibility
            bookNewList.setVisibility(View.VISIBLE);
            upcomingList.setVisibility(View.GONE);
            previousList.setVisibility(View.GONE);
            courseSearch.setVisibility(View.VISIBLE);

            updateBookNewList();
        } else if (selectedTab.getPosition() == 1) {
            bookNewList.setVisibility(View.GONE);
            upcomingList.setVisibility(View.VISIBLE);
            previousList.setVisibility(View.GONE);
            courseSearch.setVisibility(View.GONE);

            updateUpcomingList();
        } else {
            bookNewList.setVisibility(View.GONE);
            upcomingList.setVisibility(View.GONE);
            previousList.setVisibility(View.VISIBLE);
            courseSearch.setVisibility(View.GONE);

            updatePreviousList();
        }
    }

    private void updateBookNewList() {
        // Default Vars
        SearchView courseSearch = findViewById(R.id.courseSearch);
        String query = courseSearch.getQuery().toString().toLowerCase();

        // Fetching Data
        DataManager.getDataOfType(StudentDashboardActivity.this, "users", new ArrayList<>() {{
            add(new DataManager.QueryParam("role", "Tutor", DataManager.QueryType.EQUAL_TO));
            add(new DataManager.QueryParam("coursesToTeach", query, DataManager.QueryType.ARRAY_CONTAINS));
        }}, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.bookNewListContainer);
                LayoutInflater inflater = LayoutInflater.from(StudentDashboardActivity.this);

                // Clear any old templates
                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    View tutorList = inflater.inflate(R.layout.student_tutor_list, currentList, false);
                    TextView tutorNameText = tutorList.findViewById(R.id.tutorName);
                    TextView tutorRating = tutorList.findViewById(R.id.tutorRating);
                    Button viewBtn = tutorList.findViewById(R.id.view);

                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");

                    Double rating = document.getDouble("totalRating");
                    Double numRatings = document.getDouble("numRatings");
                    Number avgRating = rating != null && numRatings != null ? rating / numRatings : 0;

                    tutorNameText.setText(firstName + " " + lastName);
                    tutorRating.setText(Math.ceil(avgRating.floatValue() * 10) / 10 + " Stars");

                    viewBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(StudentDashboardActivity.this, TutorInfoActivity.class);

                        intent.putExtra("tutorId", document.getId());
                        startActivity(intent);
                        finish();
                    });

                    currentList.addView(tutorList);
                }

                if (data.isEmpty() && !query.isEmpty()) {
                    Toast.makeText(StudentDashboardActivity.this, "No tutors for this course", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(StudentDashboardActivity.this, "Could not load tutors: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updateUpcomingList() {
        // Default Vars
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) return;

        // Retrieve the user's upcoming sessions
        DataManager.getDataOfType(StudentDashboardActivity.this, "slots", new ArrayList<>(){{
            add(new DataManager.QueryParam("bookedBy", currentUser.getUid(), DataManager.QueryType.EQUAL_TO));
        }}, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.upcomingListContainer);
                LayoutInflater inflater = LayoutInflater.from(StudentDashboardActivity.this);

                // Clear any old templates
                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    // Ensure it's an upcoming slot
                    Timestamp startTime = document.getTimestamp("startTime");

                    if (startTime == null || startTime.compareTo(Timestamp.now()) < 0) {
                        continue;
                    }

                    // Create the slot
                    View timeslot = inflater.inflate(R.layout.timeslot_info, currentList, false);
                    TextView timeslotDetails = timeslot.findViewById(R.id.timeslotDetails);
                    TextView approvalStatus = timeslot.findViewById(R.id.requiresApproval);
                    Button cancelBtn = timeslot.findViewById(R.id.view);

                    Timestamp endTime = document.getTimestamp("endTime");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");
                    Boolean isPending = document.getBoolean("isPending");
                    Boolean isDenied = document.getBoolean("isDenied");

                    timeslotDetails.setText(TutorDashboardActivity.formatSlotTime(startTime, endTime));
                    approvalStatus.setText(String.format("Approval: %s", Boolean.TRUE.equals(requiresApproval) ? "Manual" : "Auto"));

                    if (Boolean.TRUE.equals(isPending)) {
                        cancelBtn.setText("Pending");
                        cancelBtn.setEnabled(false);
                    } else if (Boolean.TRUE.equals(isDenied)) {
                        cancelBtn.setText("Denied");
                        cancelBtn.setEnabled(false);
                    } else {
                        cancelBtn.setText("Cancel");

                        cancelBtn.setOnClickListener(v -> {
                            // Make sure it's > 24h
                            if (startTime.getSeconds() - Timestamp.now().getSeconds() < (86_400)) {

                                Toast.makeText(StudentDashboardActivity.this, "Cannot cancel with 24h or less to session", Toast.LENGTH_LONG).show();

                                return;
                            }

                            DataManager.updateData(StudentDashboardActivity.this, "slots", document.getId(), new HashMap<>() {{
                                put("isAvailable", true);
                                put("bookedBy", null);
                                put("isApproved", false);
                                put("studentId", null);
                                put("isDenied", false);
                                put("isPending", false);
                            }}, new DataManager.DataCallback() {
                                @Override
                                public void onSuccess(DocumentSnapshot data) {
                                    Toast.makeText(StudentDashboardActivity.this, "Successfully cancelled session", Toast.LENGTH_LONG).show();

                                    updateUpcomingList();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(StudentDashboardActivity .this, "Error while trying to cancel session: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        });
                    }

                    currentList.addView(timeslot);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(StudentDashboardActivity.this, "Error while trying to load upcoming slots", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updatePreviousList() {
        // Default Vars
        FirebaseUser currentUser = AuthManager.getCurrentUser();

        if (currentUser == null) return;

        // Retrieve the user's upcoming sessions
        DataManager.getDataOfType(StudentDashboardActivity.this, "slots", new ArrayList<>(){{
            add(new DataManager.QueryParam("bookedBy", currentUser.getUid(), DataManager.QueryType.EQUAL_TO));
        }}, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.previousListContainer);
                LayoutInflater inflater = LayoutInflater.from(StudentDashboardActivity.this);

                // Clear any old templates
                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    // Ensure it's an upcoming slot
                    Timestamp startTime = document.getTimestamp("startTime");

                    if (startTime == null || startTime.compareTo(Timestamp.now()) > 0) {
                        continue;
                    }

                    // Create the slot
                    View timeslot = inflater.inflate(R.layout.timeslot_info, currentList, false);
                    TextView timeslotDetails = timeslot.findViewById(R.id.timeslotDetails);
                    TextView approvalStatus = timeslot.findViewById(R.id.requiresApproval);
                    Button rateBtn = timeslot.findViewById(R.id.view);

                    Timestamp endTime = document.getTimestamp("endTime");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");

                    timeslotDetails.setText(TutorDashboardActivity.formatSlotTime(startTime, endTime));
                    approvalStatus.setText(String.format("Approval: %s", Boolean.TRUE.equals(requiresApproval) ? "Manual" : "Auto"));
                    rateBtn.setText("Rate");

                    rateBtn.setOnClickListener(v -> showRateDialog(document.getString("tutorId")));

                    currentList.addView(timeslot);

                    DataManager.getDataById(StudentDashboardActivity.this, "users", document.getString("tutorId"), new DataManager.DataCallback() {
                        @Override
                        public void onSuccess(DocumentSnapshot data) {
                            @SuppressWarnings("unchecked")
                            List<String> usersVoted = (List<String>) data.get("usersVoted");

                            if (usersVoted != null && usersVoted.contains(currentUser.getUid())) {
                                rateBtn.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(StudentDashboardActivity.this, "Error while trying to load upcoming slots", Toast.LENGTH_LONG).show();
            }
        });
    }
}
