package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

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

    protected void updateCurrentSelection(TabLayout.Tab selectedTab) {
        ScrollView bookNewList = findViewById(R.id.bookNewList);
        ScrollView upcomingList = findViewById(R.id.upcomingList);
        ScrollView previousList = findViewById(R.id.previousList);

        if (selectedTab.getPosition() == 0) {
            // Update visibility
            bookNewList.setVisibility(View.VISIBLE);
            upcomingList.setVisibility(View.GONE);
            previousList.setVisibility(View.GONE);

            updateBookNewList();
        }
    }

    private void updateBookNewList() {
        // Default Vars
        SearchView courseSearch = findViewById(R.id.courseSearch);
        String query = courseSearch.getQuery().toString().toLowerCase();

        // Fetching Data
        DataManager.getDataOfType(StudentDashboardActivity.this, "users", new ArrayList<>() {{
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
                    TextView highestLevelEducationText = tutorList.findViewById(R.id.highestLevelEducation);
                    Button viewBtn = tutorList.findViewById(R.id.view);

                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    String highestLevelOfStudy = document.getString("highestLevelOfStudy");

                    tutorNameText.setText(firstName + " " + lastName);
                    highestLevelEducationText.setText("Highest Level of Study: " + highestLevelOfStudy);
                    currentList.addView(tutorList);
                }
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }
}
