package com.example.otams;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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


            long startTimeMillis = startDateTime.getTimeInMillis();
            long endTimeMillis = endDateTime.getTimeInMillis();

            if (endTimeMillis <= startTimeMillis) {
                Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
                return; // Stop the function
            }else if ((endTimeMillis- startTimeMillis) % 1800000 != 0){
                Toast.makeText(this, "End time must be 30 minutes after start time.", Toast.LENGTH_SHORT).show();
                return;

            }


            String approvalMethod = isAutoApprove ? "Auto-Approve" : "Manual Approval";
            String message = "Slot Created!\n" +
                    "Start: " + startDateTime.getTime() + "\n" +
                    "End: " + endDateTime.getTime() + "\n" +
                    "Approval: " + approvalMethod;

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            finish();
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
