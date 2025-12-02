package com.example.otams;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CreateTimeslotActivity extends AppCompatActivity {

    private final Calendar startDateTime = Calendar.getInstance();
    private final Calendar endDateTime = Calendar.getInstance();
    private boolean isAutoApprove = false;

    private TextView dateText, startTimeText, endTimeText;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_timeslot);

        ImageButton backButton = findViewById(R.id.backButt);
        Button datePickerButton = findViewById(R.id.datePickerButton);
        Button startTimePickerButton = findViewById(R.id.startTimePickerButton);
        Button endTimePickerButton = findViewById(R.id.endTimePickerButton);
        RadioGroup approvalRadioGroup = findViewById(R.id.approvalRadioGroup);
        Button createSlotButton = findViewById(R.id.createSlotButton);

        dateText = findViewById(R.id.dateText);
        startTimeText = findViewById(R.id.startTimeText);
        endTimeText = findViewById(R.id.endTimeText);

        updateDateText();
        updateStartTimeText();
        updateEndTimeText();

        backButton.setOnClickListener(v -> finish());

        // DATE PICKER

        datePickerButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateTimeslotActivity.this, (view, year, month, dayOfMonth) -> {
                startDateTime.set(Calendar.YEAR, year);
                startDateTime.set(Calendar.MONTH, month);
                startDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                endDateTime.set(Calendar.YEAR, year);
                endDateTime.set(Calendar.MONTH, month);
                endDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateText();
            },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // START TIME PICKER

        startTimePickerButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateTimeslotActivity.this, (view, hourOfDay, minute) -> {
                startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startDateTime.set(Calendar.MINUTE, minute);

                updateStartTimeText();
            },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });

        // END TIME PICKER

        endTimePickerButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateTimeslotActivity.this, (view, hourOfDay, minute) -> {
                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endDateTime.set(Calendar.MINUTE, minute);

                updateEndTimeText();
            },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });

        // RADIO GROUP LOGIC

        approvalRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.autoApproveRadio) {
                isAutoApprove = true;
            } else if (checkedId == R.id.manualApprovalRadio) {
                isAutoApprove = false;
            }
        });

        // Create slot button

        createSlotButton.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            long startTimeMillis = startDateTime.getTimeInMillis();
            long endTimeMillis = endDateTime.getTimeInMillis();

            int startMinute = startDateTime.get(Calendar.MINUTE);
            int endMinute = endDateTime.get(Calendar.MINUTE);

            // Existing validation checks
            if (startDateTime.before(now)) {
                Toast.makeText(this, "Start time must be in the future.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endTimeMillis <= startTimeMillis) {
                Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
                return;
            } else if (startMinute % 30 != 0 || endMinute % 30 != 0) {
                Toast.makeText(this, "Time slot must be at 30 minute increments.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch user data first to get the ID
            DataManager.getData(CreateTimeslotActivity.this, new DataManager.DataCallback() {
                @Override
                public void onSuccess(DocumentSnapshot userData) {
                    String tutorId = userData.getId();

                    // NEW: Query existing slots for this tutor to check for overlaps
                    DataManager.getDataOfType(CreateTimeslotActivity.this, "slots", new ArrayList<>() {{
                        add(new DataManager.QueryParam("tutorId", tutorId, DataManager.QueryType.EQUAL_TO));
                    }}, new DataManager.QueryCallback() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            // Iterate through existing slots
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Timestamp existingStartTs = document.getTimestamp("startTime");
                                Timestamp existingEndTs = document.getTimestamp("endTime");

                                if (existingStartTs != null && existingEndTs != null) {
                                    long existingStartMillis = existingStartTs.toDate().getTime();
                                    long existingEndMillis = existingEndTs.toDate().getTime();

                                    // Overlap Logic: (StartA < EndB) and (EndA > StartB)
                                    if (startTimeMillis < existingEndMillis && endTimeMillis > existingStartMillis) {
                                        Toast.makeText(CreateTimeslotActivity.this, "This time slot overlaps with an existing session.", Toast.LENGTH_LONG).show();
                                        return; // Stop creation if overlap found
                                    }
                                }
                            }

                            // No overlap found, proceed to create the data
                            HashMap<String, Object> newData = new HashMap<>();
                            newData.put("startTime", startDateTime.getTime());
                            newData.put("endTime", endDateTime.getTime());
                            newData.put("isAvailable", true);
                            newData.put("requiresApproval", !isAutoApprove);
                            newData.put("tutorId", tutorId);
                            newData.put("isPending", true);

                            DataManager.createData(CreateTimeslotActivity.this, "slots", true, newData, new DataManager.DataCallback() {
                                @Override
                                public void onSuccess(DocumentSnapshot data) {
                                    Toast.makeText(CreateTimeslotActivity.this, "Session Created!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(CreateTimeslotActivity.this, TutorDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(CreateTimeslotActivity.this, "Error saving timeslot data: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(CreateTimeslotActivity.this, "Error verifying slots: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CreateTimeslotActivity.this, "Error getting tutor data: " + errorMessage, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        });
    }

    private void updateDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        dateText.setText(dateFormat.format(startDateTime.getTime()));
    }

    private void updateStartTimeText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        startTimeText.setText(timeFormat.format(startDateTime.getTime()));
    }

    private void updateEndTimeText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        endTimeText.setText(timeFormat.format(endDateTime.getTime()));
    }
}
