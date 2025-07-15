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
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonSaveTask;
    private TextView textViewDateTime;
    private Spinner spinnerCategory;
    private FirebaseFirestore db;

    private String selectedDate = "";
    private String selectedTime = "";
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initializeViews();
        setupFirestore();
        setupSpinner();
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

    private void setupSaveButton() {
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(title, description, selectedDate, selectedTime, category);

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