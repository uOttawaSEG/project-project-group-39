package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public class PendingRequestActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_pending_popup);

        TextView statusText = findViewById(R.id.status_text);

        DataManager.getData(PendingRequestActivity.this, new DataManager.DataCallback() {
            @Override
            public void onSuccess(DocumentSnapshot data) {
                boolean isDenied = Objects.equals(data.getBoolean("isDenied"), true);

                if (isDenied) {
                    statusText.setText("Denied");
                } else {
                    statusText.setText("Pending");
                }
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });

        Button logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            AuthManager.logout();
            Intent intent = new Intent(PendingRequestActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
