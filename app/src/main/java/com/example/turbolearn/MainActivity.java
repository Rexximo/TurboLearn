package com.example.turbolearn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private static final String TAG = "MainActivity";
    private static final int EDIT_TASK_REQUEST = 1001;

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private FirebaseFirestore db;
    private Spinner spinnerCategory;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "Starting MainActivity onCreate");
            setContentView(R.layout.activity_main);

            // Initialize views with null checks
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndFinish("Layout elements not found");
                return;
            }

            Log.d(TAG, "Views initialized successfully");

            // Setup Firebase with error handling
            if (!setupFirestore()) {
                Log.e(TAG, "Failed to setup Firestore");
                showErrorAndFinish("Firebase setup failed");
                return;
            }

            Log.d(TAG, "Firestore setup successful");

            setupRecyclerView();
            Log.d(TAG, "RecyclerView setup complete");

            setupSpinner();
            Log.d(TAG, "Spinner setup complete");

            setupFAB();
            Log.d(TAG, "FAB setup complete");

            // Create notification channel with try-catch
            try {
                NotificationHelper.createNotificationChannel(this);
                Log.d(TAG, "Notification channel created");
            } catch (Exception e) {
                Log.w(TAG, "Failed to create notification channel", e);
                // Continue without notification channel
            }

            loadTasks();
            Log.d(TAG, "MainActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            showErrorAndFinish("App initialization failed: " + e.getMessage());
        }
    }

    private boolean initializeViews() {
        try {
            recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
            if (recyclerViewTasks == null) {
                Log.e(TAG, "recyclerViewTasks not found in layout");
                return false;
            }

            spinnerCategory = findViewById(R.id.spinnerCategory);
            if (spinnerCategory == null) {
                Log.e(TAG, "spinnerCategory not found in layout");
                return false;
            }

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

    private void setupRecyclerView() {
        try {
            taskList = new ArrayList<>();
            taskAdapter = new TaskAdapter(taskList, this, this);
            recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewTasks.setAdapter(taskAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            showErrorAndFinish("RecyclerView setup failed: " + e.getMessage());
        }
    }

    private void setupSpinner() {
        try {
            String[] categories = {"All", "Personal", "Work", "Study", "Health", "Other"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCategory = categories[position];
                    loadTasks();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up Spinner", e);
            // Continue without spinner functionality
        }
    }

    private void setupFAB() {
        try {
            FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
            if (fabAddTask == null) {
                Log.w(TAG, "FAB not found in layout");
                return;
            }

            fabAddTask.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting AddTaskActivity", e);
                    Toast.makeText(this, "Failed to open add task screen", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FAB", e);
            // Continue without FAB
        }
    }

    private void loadTasks() {
        try {
            if (db == null) {
                Log.e(TAG, "Firestore instance is null");
                return;
            }

            Query query = db.collection("tasks").orderBy("timestamp", Query.Direction.DESCENDING);

            query.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w(TAG, "Error loading tasks", error);
                    Toast.makeText(this, "Error loading tasks: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    taskList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Task task = doc.toObject(Task.class);
                            task.setId(doc.getId());

                            if (selectedCategory.equals("All") || matchesCategory(task, selectedCategory)) {
                                taskList.add(task);
                            }
                        }
                    }

                    if (taskAdapter != null) {
                        taskAdapter.notifyDataSetChanged();
                    }

                    Log.d(TAG, "Tasks loaded successfully: " + taskList.size() + " tasks");
                } catch (Exception e) {
                    Log.e(TAG, "Error processing tasks", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadTasks", e);
        }
    }

    private boolean matchesCategory(Task task, String categoryFilter) {
        try {
            if (task.getCategory() == null) {
                return categoryFilter.equals("Other");
            }
            return task.getCategory().getDisplayName().equals(categoryFilter);
        } catch (Exception e) {
            Log.e(TAG, "Error matching category", e);
            return false;
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Finishing activity due to error: " + message);
        finish();
    }

    // Implementation of TaskAdapter.OnTaskActionListener
    @Override
    public void onEditTask(Task task) {
        try {
            Intent intent = new Intent(this, EditTaskActivity.class);
            intent.putExtra("task_id", task.getId());
            intent.putExtra("task_title", task.getTitle());
            intent.putExtra("task_description", task.getDescription());
            intent.putExtra("task_date", task.getDate());
            intent.putExtra("task_time", task.getTime());
            intent.putExtra("task_category", task.getCategoryAsString());
            intent.putExtra("task_priority", task.getPriority() != null ?
                    task.getPriority().name() : Task.Priority.MEDIUM.name());
            startActivityForResult(intent, EDIT_TASK_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error editing task", e);
            Toast.makeText(this, "Failed to edit task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteTask(Task task) {
        try {
            showDeleteConfirmationDialog(task);
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog", e);
        }
    }

    @Override
    public void onToggleTaskCompletion(Task task) {
        try {
            task.setCompleted(!task.isCompleted());

            db.collection("tasks")
                    .document(task.getId())
                    .set(task)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Task completion status updated");
                        String message = task.isCompleted() ?
                                "Task marked as completed" : "Task marked as incomplete";

                        // Use regular Toast if Toasty is not available
                        try {
                            Toasty.success(this, message, Toasty.LENGTH_SHORT, true).show();
                        } catch (Exception e) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating task completion", e);
                        try {
                            Toasty.error(this, "Failed to update task", Toasty.LENGTH_SHORT, true).show();
                        } catch (Exception ex) {
                            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                        }
                        task.setCompleted(!task.isCompleted());
                        if (taskAdapter != null) {
                            taskAdapter.notifyDataSetChanged();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error toggling task completion", e);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        try {
            showTaskDetails(task);
        } catch (Exception e) {
            Log.e(TAG, "Error showing task details", e);
        }
    }

    private void showTaskDetails(Task task) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(task.getTitle());

            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(task.getDescription()).append("\n\n");
            details.append("Due Date: ").append(task.getFormattedDueDate()).append("\n");
            details.append("Category: ").append(task.getCategory() != null ?
                    task.getCategory().getDisplayName() : "None").append("\n");
            details.append("Priority: ").append(task.getPriority() != null ?
                    task.getPriority().getDisplayName() : "None").append("\n");
            details.append("Status: ").append(task.isCompleted() ? "Completed" : "Pending").append("\n");

            if (task.isOverdue()) {
                details.append("Status: OVERDUE").append("\n");
            }

            builder.setMessage(details.toString());
            builder.setPositiveButton("Edit", (dialog, which) -> onEditTask(task));
            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

            if (!task.isCompleted()) {
                builder.setNeutralButton("Mark Complete", (dialog, which) -> {
                    onToggleTaskCompletion(task);
                    dialog.dismiss();
                });
            } else {
                builder.setNeutralButton("Mark Incomplete", (dialog, which) -> {
                    onToggleTaskCompletion(task);
                    dialog.dismiss();
                });
            }

            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing task details dialog", e);
        }
    }

    private void showDeleteConfirmationDialog(Task task) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteTask(task))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete confirmation dialog", e);
        }
    }

    private void deleteTask(Task task) {
        try {
            if (task.getId() == null || task.getId().isEmpty()) {
                Toast.makeText(this, "Error: Task ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("tasks")
                    .document(task.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Task deleted successfully");
                        try {
                            Toasty.success(this, "Task deleted successfully", Toasty.LENGTH_SHORT, true).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        taskList.remove(task);
                        if (taskAdapter != null) {
                            taskAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error deleting task", e);
                        try {
                            Toasty.error(this, "Failed to delete task", Toasty.LENGTH_SHORT, true).show();
                        } catch (Exception ex) {
                            Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting task", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK) {
                loadTasks();
                try {
                    Toasty.success(this, "Task updated successfully", Toasty.LENGTH_SHORT, true).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadTasks();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
}