package com.example.dailyreminder;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class activity_add_reminder extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button btnSave, btnPickTime, btnBack;
    private Reminder reminder;
    private int position = -1; // Default value, meaning new reminder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        // Enable back button in ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveReminder);
        btnBack = findViewById(R.id.btnBacktoMain);

        // Retrieve passed reminder and position
        if (getIntent() != null && getIntent().hasExtra("reminder")) {
            reminder = (Reminder) getIntent().getSerializableExtra("reminder");
            position = getIntent().getIntExtra("position", -1);

            // Pre-fill fields with existing data for editing
            if (reminder != null) {
                etTitle.setText(reminder.getTitle());
                etDescription.setText(reminder.getDescription());
                btnPickTime.setText(reminder.getTime());
            }
        }

        // Time picker on button click
        btnPickTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    activity_add_reminder.this,
                    (timePicker, selectedHour, selectedMinute) -> {
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        btnPickTime.setText(time);  // Set the selected time on the button
                    },
                    hour,
                    minute,
                    true  // 24-hour format
            );
            timePickerDialog.show();
        });

        // Save button logic
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String description = etDescription.getText().toString();
            String time = btnPickTime.getText().toString();  // Get time from the button

            // Validate fields
            if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
                Toast.makeText(activity_add_reminder.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // If editing an existing reminder, use the existing id
            int id = (reminder != null) ? reminder.getId() : generateNewId();  // Generate a new ID if it's a new reminder

            // Create or update reminder
            Reminder updatedReminder = new Reminder(id, title, description, time);

            // Return the updated reminder and position to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedReminder", updatedReminder);
            resultIntent.putExtra("position", position);  // Send position if editing
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Back button logic
        btnBack.setOnClickListener(v -> {
            // Simply finish the current activity to return to MainActivity
            finish();
        });
    }
    private int generateNewId() {
        // For example, use the current time to generate a unique ID
        return (int) System.currentTimeMillis();
    }
}