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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTaskActivity extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";

    private TextInputEditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonUpdateTask;
    private TextView textViewDateTime;
    private Spinner spinnerCategory, spinnerPriority;
    private FirebaseFirestore db;

    private Calendar calendar;
    private Date selectedDateTime;
    private String taskId;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        initializeViews();
        setupFirestore();
        setupSpinners();
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
        spinnerPriority = findViewById(R.id.spinnerPriority);
        calendar = Calendar.getInstance();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupSpinners() {
        setupCategorySpinner();
        setupPrioritySpinner();
    }

    private void setupCategorySpinner() {
        // Create array of category display names
        String[] categories = new String[Task.TaskCategory.values().length];
        for (int i = 0; i < Task.TaskCategory.values().length; i++) {
            categories[i] = Task.TaskCategory.values()[i].getDisplayName();
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupPrioritySpinner() {
        // Create array of priority display names
        String[] priorities = new String[Task.Priority.values().length];
        for (int i = 0; i < Task.Priority.values().length; i++) {
            priorities[i] = Task.Priority.values()[i].getDisplayName();
        }

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void setupDateTimePickers() {
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
    }

    private void loadTaskData() {
        taskId = getIntent().getStringExtra("task_id");

        if (taskId == null) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load task from Firestore
        db.collection("tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentTask = documentSnapshot.toObject(Task.class);
                        if (currentTask != null) {
                            populateTaskData();
                        }
                    } else {
                        Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading task", e);
                    Toast.makeText(this, "Failed to load task: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void populateTaskData() {
        if (currentTask == null) return;

        // Set text fields
        editTextTitle.setText(currentTask.getTitle());
        editTextDescription.setText(currentTask.getDescription());

        // Set date and time
        selectedDateTime = currentTask.getDueDate();
        updateDateTimeDisplay();

        // Set category spinner
        if (currentTask.getCategory() != null) {
            Task.TaskCategory[] categories = Task.TaskCategory.values();
            for (int i = 0; i < categories.length; i++) {
                if (categories[i] == currentTask.getCategory()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        // Set priority spinner
        if (currentTask.getPriority() != null) {
            Task.Priority[] priorities = Task.Priority.values();
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i] == currentTask.getPriority()) {
                    spinnerPriority.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showDatePicker() {
        // Use current selected date or today's date
        Calendar calendarToUse = Calendar.getInstance();
        if (selectedDateTime != null) {
            calendarToUse.setTime(selectedDateTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Keep the existing time if already set
                    if (selectedDateTime != null) {
                        Calendar existingTime = Calendar.getInstance();
                        existingTime.setTime(selectedDateTime);
                        calendar.set(Calendar.HOUR_OF_DAY, existingTime.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, existingTime.get(Calendar.MINUTE));
                    }

                    selectedDateTime = calendar.getTime();
                    updateDateTimeDisplay();
                },
                calendarToUse.get(Calendar.YEAR),
                calendarToUse.get(Calendar.MONTH),
                calendarToUse.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        // Use current selected time or current time
        Calendar calendarToUse = Calendar.getInstance();
        if (selectedDateTime != null) {
            calendarToUse.setTime(selectedDateTime);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // If no date selected, use today's date
                    if (selectedDateTime == null) {
                        calendar.setTime(new Date());
                    } else {
                        calendar.setTime(selectedDateTime);
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    selectedDateTime = calendar.getTime();
                    updateDateTimeDisplay();
                },
                calendarToUse.get(Calendar.HOUR_OF_DAY),
                calendarToUse.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        if (selectedDateTime != null) {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            textViewDateTime.setText(displayFormat.format(selectedDateTime));
        } else {
            textViewDateTime.setText("No date and time selected");
        }
    }

    private void setupUpdateButton() {
        buttonUpdateTask.setOnClickListener(v -> updateTask());
    }

    private void updateTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Get selected category and priority
        Task.TaskCategory selectedCategory = Task.TaskCategory.values()[spinnerCategory.getSelectedItemPosition()];
        Task.Priority selectedPriority = Task.Priority.values()[spinnerPriority.getSelectedItemPosition()];

        // Validasi input
        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        if (selectedDateTime == null) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(taskId)) {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button saat update sedang berlangsung
        buttonUpdateTask.setEnabled(false);
        buttonUpdateTask.setText("Updating...");

        // Update current task object
        try {
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setDueDate(selectedDateTime);
            currentTask.setCategory(selectedCategory);
            currentTask.setPriority(selectedPriority);

            // Update task di Firestore
            db.collection("tasks")
                    .document(taskId)
                    .set(currentTask)
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
        } catch (Exception e) {
            Log.w(TAG, "Error updating task data", e);
            Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // Enable button kembali jika gagal
            buttonUpdateTask.setEnabled(true);
            buttonUpdateTask.setText("Update Task");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}