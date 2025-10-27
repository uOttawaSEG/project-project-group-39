package com.example.otams;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admindashboard);

        // Default Vars
        TabLayout listSelection = findViewById(R.id.listSelection);

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
            Intent intent = new Intent(AdminDashboardActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    protected void updateCurrentSelection(TabLayout.Tab selectedTab) {
        ScrollView currentList = findViewById(R.id.currentList);
        ScrollView deniedList = findViewById(R.id.deniedList);
        ScrollView approvedList = findViewById(R.id.approvedList);

        if (selectedTab.getPosition() == 0) {
            // Update visibility
            currentList.setVisibility(View.VISIBLE);
            deniedList.setVisibility(View.GONE);
            approvedList.setVisibility(View.GONE);

            updateCurrentList();
        } else if (selectedTab.getPosition() == 1) {
            // Update visibility
            currentList.setVisibility(View.GONE);
            deniedList.setVisibility(View.VISIBLE);
            approvedList.setVisibility(View.GONE);

            updateDeniedList();
        } else {
            // Update visibility
            currentList.setVisibility(View.GONE);
            deniedList.setVisibility(View.GONE);
            approvedList.setVisibility(View.VISIBLE);

            updateApprovedList();
        }
    }

    private void showUserInfoDialog(QueryDocumentSnapshot document) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.user_info_popup);

        TextView nameText = dialog.findViewById(R.id.popup_name);
        TextView emailText = dialog.findViewById(R.id.popup_email);
        TextView roleText = dialog.findViewById(R.id.popup_role);
        Button closeButton = dialog.findViewById(R.id.popup_close_button);
        TextView phoneText = dialog.findViewById(R.id.popup_phone);
        TextView programText = dialog.findViewById(R.id.popup_program);
        TextView highestLevelOfStudyText = dialog.findViewById(R.id.popup_highestLevelOfStudy);
        TextView coursesToTeachText = dialog.findViewById(R.id.popup_coursesToTeach);

        String firstName = document.getString("firstName");
        String lastName = document.getString("lastName");
        String email = document.getString("email");
        String role = document.getString("role");
        String phone = document.getString("phone");
        String program = document.getString("program");
        String highestLevelOfStudy = document.getString("highestLevelOfStudy");
        String coursesToTeach = document.getString("coursesToTeach");

        if (role != null && role.equals("Tutor")) {
            programText.setVisibility(View.GONE);
        } else {
            highestLevelOfStudyText.setVisibility(View.GONE);
            coursesToTeachText.setVisibility(View.GONE);
        }

        phoneText.setText("Phone: " + phone);
        programText.setText("Program: " + program);
        nameText.setText(firstName + " " + lastName);
        emailText.setText("Email: " + email);
        roleText.setText("Role: " + role);
        highestLevelOfStudyText.setText("Highest Level of Study: " + highestLevelOfStudy);
        coursesToTeachText.setText("Courses: " + coursesToTeach);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    protected void updateCurrentList() {
        DataManager.getDataOfType(AdminDashboardActivity.this, "isPending", true, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout currentList = findViewById(R.id.currentListContainer);
                LayoutInflater inflater = LayoutInflater.from(AdminDashboardActivity.this);

                // Clear any old templates
                currentList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    View requestRow = inflater.inflate(R.layout.current_request_row, currentList, false);
                    TextView textViewName = requestRow.findViewById(R.id.row_request_text);
                    Button approveBtn = requestRow.findViewById(R.id.row_approve_button);
                    Button denyBtn = requestRow.findViewById(R.id.row_deny_button);

                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");

                    textViewName.setText(firstName + " " + lastName);
                    textViewName.setOnClickListener(v -> showUserInfoDialog(document));

                    approveBtn.setOnClickListener(v -> {
                        DataManager.updateData(AdminDashboardActivity.this, document.getId(), new HashMap<String, Object>() {{
                            put("isPending", false);
                            put("isDenied", false); // just in case it was denied before
                            put("isAccepted", true);
                        }}, new DataManager.DataCallback() {
                            @Override
                            public void onSuccess(DocumentSnapshot data) {
                                Toast.makeText(AdminDashboardActivity.this, "Request approved", Toast.LENGTH_LONG).show();

                                updateCurrentList();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(AdminDashboardActivity.this, "Error while trying to approve request: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });

                    denyBtn.setOnClickListener(v -> {
                        DataManager.updateData(AdminDashboardActivity.this, document.getId(), new HashMap<String, Object>() {{
                            put("isPending", false);
                            put("isDenied", true);
                            put("isAccepted", false);
                        }}, new DataManager.DataCallback() {
                            @Override
                            public void onSuccess(DocumentSnapshot data) {
                                Toast.makeText(AdminDashboardActivity.this, "Request denied", Toast.LENGTH_LONG).show();

                                updateCurrentList();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(AdminDashboardActivity.this, "Error while trying to deny request: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });

                    currentList.addView(requestRow);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error while trying to load requests", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updateDeniedList() {
        DataManager.getDataOfType(AdminDashboardActivity.this, "isDenied", true, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout deniedList = findViewById(R.id.deniedListContainer);
                LayoutInflater inflater = LayoutInflater.from(AdminDashboardActivity.this);

                // Clear any old templates
                deniedList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    View requestRow = inflater.inflate(R.layout.current_request_row, deniedList, false);
                    TextView textViewName = requestRow.findViewById(R.id.row_request_text);
                    Button approveBtn = requestRow.findViewById(R.id.row_approve_button);
                    Button denyBtn = requestRow.findViewById(R.id.row_deny_button);

                    denyBtn.setVisibility(View.INVISIBLE);

                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");

                    textViewName.setText(firstName + " " + lastName);
                    textViewName.setOnClickListener(v -> showUserInfoDialog(document));

                    approveBtn.setOnClickListener(v -> {
                        DataManager.updateData(AdminDashboardActivity.this, document.getId(), new HashMap<String, Object>() {{
                            put("isPending", false);
                            put("isDenied", false);
                            put("isAccepted", true);
                        }}, new DataManager.DataCallback() {
                            @Override
                            public void onSuccess(DocumentSnapshot data) {
                                Toast.makeText(AdminDashboardActivity.this, "Request approved", Toast.LENGTH_LONG).show();

                                updateDeniedList();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(AdminDashboardActivity.this, "Error while trying to approve request: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    });

                    deniedList.addView(requestRow);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error while trying to load requests", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void updateApprovedList() {
        DataManager.getDataOfType(AdminDashboardActivity.this, "isAccepted", true, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                // Default Vars
                LinearLayout approvedList = findViewById(R.id.approvedListContainer);
                LayoutInflater inflater = LayoutInflater.from(AdminDashboardActivity.this);

                // Clear any old templates
                approvedList.removeAllViews();

                // Add in all the templates
                for (QueryDocumentSnapshot document : data) {
                    View requestRow = inflater.inflate(R.layout.current_request_row, approvedList, false);
                    TextView textViewName = requestRow.findViewById(R.id.row_request_text);
                    Button approveBtn = requestRow.findViewById(R.id.row_approve_button);
                    Button denyBtn = requestRow.findViewById(R.id.row_deny_button);

                    approveBtn.setVisibility(View.INVISIBLE);
                    denyBtn.setVisibility(View.INVISIBLE);

                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");

                    textViewName.setText(firstName + " " + lastName);
                    textViewName.setOnClickListener(v -> showUserInfoDialog(document));
                    approvedList.addView(requestRow);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error while trying to load requests", Toast.LENGTH_LONG).show();
            }
        });
    }
}
