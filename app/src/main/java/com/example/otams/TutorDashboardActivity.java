package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TutorDashboardActivity extends AppCompatActivity {
    private String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutordashboard);

        // Default Vars
        TabLayout listSelection = findViewById(R.id.listSelection);

        // Load the tutor's id (to retrieve all slots)
        DataManager.getData(TutorDashboardActivity.this, new DataManager.DataCallback() {
            @Override
            public void onSuccess(DocumentSnapshot data) {
                tutorId = data.getId();

                TabLayout.Tab initialTab = listSelection.getTabAt(listSelection.getSelectedTabPosition());

                if (initialTab != null) {
                    updateCurrentSelection(initialTab);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "[Critical] Could not fetch tutor ID", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(TutorDashboardActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Button createNewTimeSlotButton = findViewById(R.id.createTimeslotButton);
        createNewTimeSlotButton.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, CreateTimeslotActivity.class);
            startActivity(intent);
        });
    }

    public String formatSlotTime(Timestamp startTime, Timestamp endTime) {
        if (startTime == null || endTime == null) {
            return "N/A";
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM. dd", Locale.ENGLISH);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma", Locale.ENGLISH);
        Date startDate = startTime.toDate();
        Date endDate = endTime.toDate();

        String formattedDate = dateFormatter.format(startDate);
        String formattedStartTime = timeFormatter.format(startDate).toUpperCase();
        String formattedEndTime = timeFormatter.format(endDate).toUpperCase();

        return String.format("%s | %s - %s", formattedDate, formattedStartTime, formattedEndTime);
    }

    protected void updateCurrentSelection(TabLayout.Tab selectedTab) {
        ScrollView upcomingList = findViewById(R.id.upcomingList);
        ScrollView pastList = findViewById(R.id.pastList);
        ScrollView timeslotsList = findViewById(R.id.timeslotsList);
        ScrollView pendingList = findViewById(R.id.pendingRequestsList);

        Button createNewTimeSlot = findViewById(R.id.createTimeslotButton);

        if (selectedTab.getPosition() == 0) { //Upcoming
            // Update visibility
            upcomingList.setVisibility(View.VISIBLE);
            pastList.setVisibility(View.GONE);
            timeslotsList.setVisibility(View.GONE);
            pendingList.setVisibility(View.GONE);
            createNewTimeSlot.setVisibility(View.GONE);

            updateUpcomingList();
        } else if (selectedTab.getPosition() == 1) { // Past
            // Update visibility
            upcomingList.setVisibility(View.GONE);
            pastList.setVisibility(View.VISIBLE);
            timeslotsList.setVisibility(View.GONE);
            pendingList.setVisibility(View.GONE);
            createNewTimeSlot.setVisibility(View.GONE);

            updatePastList();
        } else if (selectedTab.getPosition() == 2) { // Timeslots
            // Update visibility
            upcomingList.setVisibility(View.GONE);
            pastList.setVisibility(View.GONE);
            timeslotsList.setVisibility(View.VISIBLE);
            pendingList.setVisibility(View.GONE);
            createNewTimeSlot.setVisibility(View.VISIBLE);

            updateTimeslotsList();
        } else if (selectedTab.getPosition() == 3) { // Pending
            // Update visibility
            upcomingList.setVisibility(View.GONE);
            pastList.setVisibility(View.GONE);
            timeslotsList.setVisibility(View.GONE);
            pendingList.setVisibility(View.VISIBLE);
            createNewTimeSlot.setVisibility(View.GONE);

            updatePendingList();
        }
    }

    protected void updateUpcomingList() {
        DataManager.getDataOfType(TutorDashboardActivity.this, "slots", "tutorId", tutorId, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.upcomingListContainer);
                LayoutInflater inflater = LayoutInflater.from(TutorDashboardActivity.this);

                // Clear any old templates
                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    // Ensure it's an upcoming slot
                    Timestamp startTime = document.getTimestamp("startTime");
                    Boolean isAvailable = document.getBoolean("isAvailable");

                    if (startTime == null || startTime.compareTo(Timestamp.now()) < 0 || Boolean.FALSE.equals(isAvailable)) {
                        continue;
                    }

                    // Create the slot
                    View timeslot = inflater.inflate(R.layout.timeslot_info, currentList, false);
                    TextView timeslotDetails = timeslot.findViewById(R.id.timeslot_details);
                    TextView approvalStatus = timeslot.findViewById(R.id.timeslot_approval_status);
                    Button deleteBtn = timeslot.findViewById(R.id.row_delete_button);

                    Timestamp endTime = document.getTimestamp("endTime");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");

                    timeslotDetails.setText(formatSlotTime(startTime, endTime));
                    approvalStatus.setText(String.format("Approval: %s", Boolean.TRUE.equals(requiresApproval) ? "Manual" : "Auto"));

                    deleteBtn.setOnClickListener(v -> DataManager.deleteData(TutorDashboardActivity.this, "slots", document.getId(), new DataManager.UpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(TutorDashboardActivity.this, "Successfully deleted slot", Toast.LENGTH_LONG).show();

                            updateUpcomingList();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(TutorDashboardActivity.this, "Error while trying to delete slot: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }));

                    currentList.addView(timeslot);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "Error while trying to load upcoming slots", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updatePastList() {
        DataManager.getDataOfType(TutorDashboardActivity.this, "slots", "tutorId", tutorId, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.pastListContainer);
                LayoutInflater inflater = LayoutInflater.from(TutorDashboardActivity.this);

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
                    TextView timeslotDetails = timeslot.findViewById(R.id.timeslot_details);
                    TextView approvalStatus = timeslot.findViewById(R.id.timeslot_approval_status);
                    Button deleteBtn = timeslot.findViewById(R.id.row_delete_button);

                    Timestamp endTime = document.getTimestamp("endTime");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");

                    timeslotDetails.setText(formatSlotTime(startTime, endTime));
                    approvalStatus.setText(String.format("Approval: %s", Boolean.TRUE.equals(requiresApproval) ? "Manual" : "Auto"));

                    deleteBtn.setVisibility(View.GONE);

                    currentList.addView(timeslot);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "Error while trying to load past slots", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updateTimeslotsList() {
        DataManager.getDataOfType(TutorDashboardActivity.this, "slots", "tutorId", tutorId, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.timeslotsListContainer);
                LayoutInflater inflater = LayoutInflater.from(TutorDashboardActivity.this);

                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    // Ensure it's an upcoming slot
                    Timestamp startTime = document.getTimestamp("startTime");
                    Boolean isAvailable = document.getBoolean("isAvailable");
                    String bookedBy = document.getString("bookedBy");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");
                    Boolean isApproved = document.getBoolean("isApproved");

                    if (startTime == null || startTime.compareTo(Timestamp.now()) < 0 || Boolean.TRUE.equals(isAvailable) || bookedBy == null) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(requiresApproval) && !Boolean.TRUE.equals(isApproved)) {
                        continue;
                    }

                    // Retrieve the data first
                    DataManager.getUserData(TutorDashboardActivity.this, bookedBy, new DataManager.DataCallback() {
                       @Override
                       public void onSuccess(DocumentSnapshot userData) {
                           // Create the slot
                           View timeslot = inflater.inflate(R.layout.sessions_info, currentList, false);
                           TextView timeslotDetails = timeslot.findViewById(R.id.session_date_time);
                           TextView studentDetails = timeslot.findViewById(R.id.session_student_info);
                           TextView approvalStatus = timeslot.findViewById(R.id.session_status_indicator);

                           Timestamp endTime = document.getTimestamp("endTime");
                           String formattedTime = formatSlotTime(startTime, endTime);

                           timeslotDetails.setText(formattedTime);
                           studentDetails.setText(userData.getString("firstName") + " " + userData.getString("lastName"));
                           approvalStatus.setText("Status: Approved");

                           timeslot.setOnClickListener(click -> {
                               Intent intent = new Intent(TutorDashboardActivity.this, SessionConfirmedActivity.class);
                               intent.putExtra("bookedBy", bookedBy);
                               intent.putExtra("formattedTime", formattedTime);
                               intent.putExtra("sessionId", document.getId());
                               startActivity(intent);
                               finish();
                           });

                           currentList.addView(timeslot);
                       }

                       @Override
                       public void onFailure(String errorMessage) {
                           Toast.makeText(TutorDashboardActivity.this, "Could not fetch booker data " + errorMessage, Toast.LENGTH_LONG).show();
                       }
                    });
                }
            }
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "Error while trying to load timeslots", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updatePendingList() {
        DataManager.getDataOfType(TutorDashboardActivity.this, "slots", "tutorId", tutorId, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.pendingRequestsListContainer);
                LayoutInflater inflater = LayoutInflater.from(TutorDashboardActivity.this);

                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    // Ensure it's an upcoming slot
                    Timestamp startTime = document.getTimestamp("startTime");
                    Boolean isAvailable = document.getBoolean("isAvailable");
                    String bookedBy = document.getString("bookedBy");
                    Boolean requiresApproval = document.getBoolean("requiresApproval");
                    Boolean isApproved = document.getBoolean("isApproved");

                    if (startTime == null || startTime.compareTo(Timestamp.now()) < 0 || Boolean.TRUE.equals(isAvailable) || bookedBy == null) {
                        continue;
                    }

                    if (Boolean.FALSE.equals(requiresApproval) || Boolean.TRUE.equals(isApproved)) {
                        continue;
                    }

                    // Retrieve the data first
                    DataManager.getUserData(TutorDashboardActivity.this, bookedBy, new DataManager.DataCallback() {
                        @Override
                        public void onSuccess(DocumentSnapshot userData) {
                            // Create the slot
                            View timeslot = inflater.inflate(R.layout.sessions_info, currentList, false);
                            TextView timeslotDetails = timeslot.findViewById(R.id.session_date_time);
                            TextView studentDetails = timeslot.findViewById(R.id.session_student_info);
                            TextView approvalStatus = timeslot.findViewById(R.id.session_status_indicator);

                            Timestamp endTime = document.getTimestamp("endTime");
                            String formattedTime = formatSlotTime(startTime, endTime);

                            timeslotDetails.setText(formattedTime);
                            studentDetails.setText(userData.getString("firstName") + " " + userData.getString("lastName"));
                            approvalStatus.setText("Click to review");

                            timeslot.setOnClickListener(click -> {
                                Intent intent = new Intent(TutorDashboardActivity.this, SessionRequestActivity.class);
                                intent.putExtra("bookedBy", bookedBy);
                                intent.putExtra("formattedTime", formattedTime);
                                intent.putExtra("sessionId", document.getId());
                                startActivity(intent);
                                finish();
                            });

                            currentList.addView(timeslot);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(TutorDashboardActivity.this, "Could not fetch booker data " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "Error while trying to load timeslots", Toast.LENGTH_LONG).show();
            }
        });
    }
}