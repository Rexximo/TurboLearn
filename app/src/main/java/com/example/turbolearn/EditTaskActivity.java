package com.example.turbolearn;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTaskActivity extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";

    private TextInputEditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonUpdateTask;
    private TextView textViewDateTime;
    private Spinner spinnerCategory;
    private FirebaseFirestore db;

    private String selectedDate = "";
    private String selectedTime = "";
    private Calendar calendar;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        initializeViews();
        setupFirestore();
        setupSpinner();
        setupDateTimePickers();
        setupUpdateButton();
        loadTaskData();
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonUpdateTask = findViewById(R.id.buttonUpdateTask);
        textViewDateTime = findViewById(R.id.textViewDateTime);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        calendar = Calendar.getInstance();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupSpinner() {
        String[] categories = {"Easy", "Hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
    }

    private void loadTaskData() {
        taskId = getIntent().getStringExtra("task_id");
        String title = getIntent().getStringExtra("task_title");
        String description = getIntent().getStringExtra("task_description");
        selectedDate = getIntent().getStringExtra("task_date");
        selectedTime = getIntent().getStringExtra("task_time");
        String category = getIntent().getStringExtra("task_category");

        // Set default values if data is null
        if (title != null) editTextTitle.setText(title);
        if (description != null) editTextDescription.setText(description);
        if (selectedDate == null) selectedDate = "";
        if (selectedTime == null) selectedTime = "";

        updateDateTimeDisplay();

        // Set spinner selection
        String[] categories = {"Easy", "Hard"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = dateFormat.format(calendar.getTime());
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    selectedTime = timeFormat.format(calendar.getTime());
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        String dateTime = "";
        if (!selectedDate.isEmpty()) {
            dateTime += selectedDate;
        }
        if (!selectedTime.isEmpty()) {
            if (!dateTime.isEmpty()) {
                dateTime += " ";
            }
            dateTime += selectedTime;
        }

        if (dateTime.isEmpty()) {
            textViewDateTime.setText("No date and time selected");
        } else {
            textViewDateTime.setText(dateTime);
        }
    }

    private void setupUpdateButton() {
        buttonUpdateTask.setOnClickListener(v -> updateTask());
    }

    private void updateTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // Validasi input
        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("Description is required");
            editTextDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(taskId)) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button saat update sedang berlangsung
        buttonUpdateTask.setEnabled(false);
        buttonUpdateTask.setText("Updating...");

        // Buat map data untuk update
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", title);
        taskData.put("description", description);
        taskData.put("date", selectedDate);
        taskData.put("time", selectedTime);
        taskData.put("category", category);
        taskData.put("updatedAt", System.currentTimeMillis());

        // Update task di Firestore
        db.collection("tasks")
                .document(taskId)
                .update(taskData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task updated successfully");
                    Toast.makeText(EditTaskActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();

                    // Kembali ke activity sebelumnya
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating task", e);
                    Toast.makeText(EditTaskActivity.this, "Failed to update task: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Enable button kembali jika gagal
                    buttonUpdateTask.setEnabled(true);
                    buttonUpdateTask.setText("Update Task");
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}