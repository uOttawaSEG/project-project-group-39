package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
//        ScrollView deniedList = findViewById(R.id.deniedList);
//        ScrollView approvedList = findViewById(R.id.approvedList);

        // Current List
        DataManager.getDataOfType(AdminDashboardActivity.this, "isPending", true, new DataManager.QueryCallback() {
            @Override
            public void onSuccess(QuerySnapshot data) {
                updateCurrentList(data);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AdminDashboardActivity.this, "Error while trying to load requests", Toast.LENGTH_LONG).show();
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

    protected void updateCurrentList(QuerySnapshot data) {
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
            currentList.addView(requestRow);

            approveBtn.setOnClickListener(v -> {
                DataManager.updateData(AdminDashboardActivity.this, document.getId(), new HashMap<String, Object>() {{
                    put("isPending", false);
                    put("isDenied", false); // just in case it was denied before
                    put("isAccepted", true);
                }}, new DataManager.DataCallback() {
                    @Override
                    public void onSuccess(DocumentSnapshot data) {
                        Toast.makeText(AdminDashboardActivity.this, "Request approved", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(AdminDashboardActivity.this, "Error while trying to approve request: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }
}
