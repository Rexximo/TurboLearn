package com.example.turbolearn;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AddTaskActivity extends AppCompatActivity {

    private static final String TAG = "AddTaskActivity";

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

        try {
            Log.d(TAG, "Starting AddTaskActivity onCreate");
            setContentView(R.layout.activity_add_task);

            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndFinish("Layout elements not found");
                return;
            }

            if (!setupFirestore()) {
                Log.e(TAG, "Failed to setup Firestore");
                showErrorAndFinish("Firebase setup failed");
                return;
            }

            setupSpinners();
            setupDateTimePickers();
            setupSaveButton();

            Log.d(TAG, "AddTaskActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            showErrorAndFinish("App initialization failed: " + e.getMessage());
        }
    }

    private boolean initializeViews() {
        try {
            editTextTitle = findViewById(R.id.editTextTitle);
            if (editTextTitle == null) {
                Log.e(TAG, "editTextTitle not found in layout");
                return false;
            }

            editTextDescription = findViewById(R.id.editTextDescription);
            if (editTextDescription == null) {
                Log.e(TAG, "editTextDescription not found in layout");
                return false;
            }

            buttonSelectDate = findViewById(R.id.buttonSelectDate);
            if (buttonSelectDate == null) {
                Log.e(TAG, "buttonSelectDate not found in layout");
                return false;
            }

            buttonSelectTime = findViewById(R.id.buttonSelectTime);
            if (buttonSelectTime == null) {
                Log.e(TAG, "buttonSelectTime not found in layout");
                return false;
            }

            buttonSaveTask = findViewById(R.id.buttonSaveTask);
            if (buttonSaveTask == null) {
                Log.e(TAG, "buttonSaveTask not found in layout");
                return false;
            }

            textViewDateTime = findViewById(R.id.textViewDateTime);
            if (textViewDateTime == null) {
                Log.e(TAG, "textViewDateTime not found in layout");
                return false;
            }

            spinnerCategory = findViewById(R.id.spinnerCategory);
            if (spinnerCategory == null) {
                Log.e(TAG, "spinnerCategory not found in layout");
                return false;
            }

            spinnerPriority = findViewById(R.id.spinnerPriority);
            if (spinnerPriority == null) {
                Log.e(TAG, "spinnerPriority not found in layout");
                return false;
            }

            calendar = Calendar.getInstance();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            return false;
        }
    }

    private boolean setupFirestore() {
        try {
            db = FirebaseFirestore.getInstance();
            if (db == null) {
                Log.e(TAG, "Failed to get Firestore instance");
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Firestore", e);
            return false;
        }
    }

    private void setupSpinners() {
        try {
            setupCategorySpinner();
            setupPrioritySpinner();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinners", e);
        }
    }

    private void setupCategorySpinner() {
        try {
            Task.TaskCategory[] categories = Task.TaskCategory.values();
            String[] categoryNames = new String[categories.length];

            for (int i = 0; i < categories.length; i++) {
                categoryNames[i] = categories[i].getDisplayName();
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, categoryNames);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(categoryAdapter);

            // Set default selection to OTHER
            spinnerCategory.setSelection(getIndexOfCategory(Task.TaskCategory.OTHER));

        } catch (Exception e) {
            Log.e(TAG, "Error setting up category spinner", e);
        }
    }

    private void setupPrioritySpinner() {
        try {
            Task.Priority[] priorities = Task.Priority.values();
            String[] priorityNames = new String[priorities.length];

            for (int i = 0; i < priorities.length; i++) {
                priorityNames[i] = priorities[i].getDisplayName();
            }

            ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, priorityNames);
            priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPriority.setAdapter(priorityAdapter);

            // Set default selection to MEDIUM
            spinnerPriority.setSelection(getIndexOfPriority(Task.Priority.MEDIUM));

        } catch (Exception e) {
            Log.e(TAG, "Error setting up priority spinner", e);
        }
    }

    private int getIndexOfCategory(Task.TaskCategory category) {
        try {
            Task.TaskCategory[] categories = Task.TaskCategory.values();
            for (int i = 0; i < categories.length; i++) {
                if (categories[i] == category) {
                    return i;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category index", e);
        }
        return 0; // Return first item if not found
    }

    private int getIndexOfPriority(Task.Priority priority) {
        try {
            Task.Priority[] priorities = Task.Priority.values();
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i] == priority) {
                    return i;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting priority index", e);
        }
        return 0; // Return first item if not found
    }

    private void setupDateTimePickers() {
        try {
            buttonSelectDate.setOnClickListener(v -> showDatePicker());
            buttonSelectTime.setOnClickListener(v -> showTimePicker());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up date time pickers", e);
        }
    }

    private void showDatePicker() {
        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        try {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Preserve existing time if already set
                            if (selectedDateTime != null) {
                                Calendar existingTime = Calendar.getInstance();
                                existingTime.setTime(selectedDateTime);
                                calendar.set(Calendar.HOUR_OF_DAY, existingTime.get(Calendar.HOUR_OF_DAY));
                                calendar.set(Calendar.MINUTE, existingTime.get(Calendar.MINUTE));
                            } else {
                                // Set default time to current time if no time selected
                                Calendar now = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                                calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                            }

                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            selectedDateTime = calendar.getTime();
                            updateDateTimeDisplay();

                        } catch (Exception e) {
                            Log.e(TAG, "Error in date picker callback", e);
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker", e);
            Toast.makeText(this, "Error opening date picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimePicker() {
        try {
            // Use current time as default if no date/time selected
            if (selectedDateTime == null) {
                calendar.setTime(new Date());
            } else {
                calendar.setTime(selectedDateTime);
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        try {
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

                        } catch (Exception e) {
                            Log.e(TAG, "Error in time picker callback", e);
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // Use 24 hour format
            );
            timePickerDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing time picker", e);
            Toast.makeText(this, "Error opening time picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateTimeDisplay() {
        try {
            if (selectedDateTime != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textViewDateTime.setText(displayFormat.format(selectedDateTime));
            } else {
                textViewDateTime.setText("No date and time selected");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating date time display", e);
            textViewDateTime.setText("Error displaying date/time");
        }
    }

    private void setupSaveButton() {
        try {
            buttonSaveTask.setOnClickListener(v -> saveTask());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up save button", e);
        }
    }

    private void saveTask() {
        try {
            // Validate user authentication
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                showToast("User not logged in", false);
                return;
            }

            // Get and validate input data
            String title = editTextTitle.getText() != null ?
                    editTextTitle.getText().toString().trim() : "";
            String description = editTextDescription.getText() != null ?
                    editTextDescription.getText().toString().trim() : "";

            // Validate required fields
            if (title.isEmpty()) {
                editTextTitle.setError("Title is required");
                editTextTitle.requestFocus();
                return;
            }

            if (selectedDateTime == null) {
                showToast("Please select date and time", false);
                return;
            }

            // Validate that the selected date is not in the past
            if (selectedDateTime.before(new Date())) {
                showToast("Please select a future date and time", false);
                return;
            }

            // Get selected category and priority
            Task.TaskCategory selectedCategory = Task.TaskCategory.OTHER;
            Task.Priority selectedPriority = Task.Priority.MEDIUM;

            try {
                if (spinnerCategory.getSelectedItemPosition() >= 0 &&
                        spinnerCategory.getSelectedItemPosition() < Task.TaskCategory.values().length) {
                    selectedCategory = Task.TaskCategory.values()[spinnerCategory.getSelectedItemPosition()];
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting selected category, using default", e);
            }

            try {
                if (spinnerPriority.getSelectedItemPosition() >= 0 &&
                        spinnerPriority.getSelectedItemPosition() < Task.Priority.values().length) {
                    selectedPriority = Task.Priority.values()[spinnerPriority.getSelectedItemPosition()];
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting selected priority, using default", e);
            }

            String userId = currentUser.getUid();

            // Create task using the constructor that includes userId
            Task task = new Task(userId, title, description, selectedDateTime, selectedCategory, selectedPriority);

            // Convert task to Map for Firestore (alternative to direct object)
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("userId", task.getUserId());
            taskData.put("title", task.getTitle());
            taskData.put("description", task.getDescription());
            taskData.put("dueDate", task.getDueDate());
            taskData.put("category", task.getCategory().name());
            taskData.put("priority", task.getPriority().name());
            taskData.put("completed", task.isCompleted());
            taskData.put("timestamp", task.getTimestamp());
            taskData.put("createdAt", task.getCreatedAt());
            taskData.put("updatedAt", task.getUpdatedAt());

            // Disable save button to prevent double submission
            buttonSaveTask.setEnabled(false);
            buttonSaveTask.setText("Saving...");

            // Save to Firestore
            db.collection("tasks")
                    .add(taskData)
                    .addOnSuccessListener(documentReference -> {
                        try {
                            Log.d(TAG, "Task saved successfully with ID: " + documentReference.getId());

                            // Schedule notification if NotificationHelper is available
                            try {
                                NotificationHelper.scheduleNotification(this, task, documentReference.getId());
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to schedule notification", e);
                            }

                            showToast("Task saved successfully", true);

                            // Set result and finish activity
                            setResult(RESULT_OK);
                            finish();

                        } catch (Exception e) {
                            Log.e(TAG, "Error in success callback", e);
                            resetSaveButton();
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            Log.e(TAG, "Error saving task", e);
                            showToast("Error saving task: " + e.getMessage(), false);
                            resetSaveButton();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error in failure callback", ex);
                            resetSaveButton();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in saveTask method", e);
            showToast("Unexpected error occurred", false);
            resetSaveButton();
        }
    }

    private void resetSaveButton() {
        try {
            buttonSaveTask.setEnabled(true);
            buttonSaveTask.setText("Save Task");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting save button", e);
        }
    }

    private void showToast(String message, boolean isSuccess) {
        try {
            // Use Toasty if available, otherwise use regular Toast
            if (isSuccess) {
                try {
                    Toasty.success(this, message, Toasty.LENGTH_SHORT, true).show();
                } catch (Exception e) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    Toasty.error(this, message, Toasty.LENGTH_SHORT, true).show();
                } catch (Exception e) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
            // Fallback to system toast
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorAndFinish(String message) {
        showToast(message, false);
        Log.e(TAG, "Finishing activity due to error: " + message);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "AddTaskActivity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
}