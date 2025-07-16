package com.example.turbolearn;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
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
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonSaveTask;
    private TextView textViewDateTime;
    private Spinner spinnerCategory, spinnerPriority;
    private FirebaseFirestore db;

    private Calendar calendar;
    private Date selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initializeViews();
        setupFirestore();
        setupSpinners();
        setupDateTimePickers();
        setupSaveButton();
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
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

        // Set default to "Other"
        spinnerCategory.setSelection(getIndexOfCategory(Task.TaskCategory.OTHER));
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

        // Set default to "Medium"
        spinnerPriority.setSelection(getIndexOfPriority(Task.Priority.MEDIUM));
    }

    private int getIndexOfCategory(Task.TaskCategory category) {
        Task.TaskCategory[] categories = Task.TaskCategory.values();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == category) {
                return i;
            }
        }
        return 0;
    }

    private int getIndexOfPriority(Task.Priority priority) {
        Task.Priority[] priorities = Task.Priority.values();
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i] == priority) {
                return i;
            }
        }
        return 0;
    }

    private void setupDateTimePickers() {
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
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
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
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

    private void setupSaveButton() {
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Get selected category
        Task.TaskCategory selectedCategory = Task.TaskCategory.values()[spinnerCategory.getSelectedItemPosition()];

        // Get selected priority
        Task.Priority selectedPriority = Task.Priority.values()[spinnerPriority.getSelectedItemPosition()];

        // Validate input
        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }

        if (selectedDateTime == null) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create task using the proper constructor
        Task task = new Task(title, description, selectedDateTime, selectedCategory, selectedPriority);

        // Save to Firestore
        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();

                    // Schedule notification
                    NotificationHelper.scheduleNotification(this, task, documentReference.getId());

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving task: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}