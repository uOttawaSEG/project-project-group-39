package com.example.otams;

import android.content.Intent;
import android.os.Build;
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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
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

        if (selectedTab.getPosition() == 0) {
            // Update visibility
            upcomingList.setVisibility(View.VISIBLE);
            pastList.setVisibility(View.GONE);
            timeslotsList.setVisibility(View.GONE);
            pendingList.setVisibility(View.GONE);

            updateUpcomingList();
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

                    if (startTime == null || startTime.compareTo(Timestamp.now()) < 0) {
                        return;
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

                    deleteBtn.setOnClickListener(v -> {

                    });

                    currentList.addView(timeslot);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(TutorDashboardActivity.this, "Error while trying to load upcoming slots", Toast.LENGTH_LONG).show();
            }
        });
    }
}
