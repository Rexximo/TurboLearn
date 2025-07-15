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
        setContentView(R.layout.activity_main);

        initializeViews();
        setupFirestore();
        setupRecyclerView();
        setupSpinner();
        setupFAB();
        loadTasks();

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);
    }

    private void initializeViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        spinnerCategory = findViewById(R.id.spinnerCategory);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this, this); // Pass listener
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupSpinner() {
        String[] categories = {"All", "Easy", "Hard"};
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
    }

    private void setupFAB() {
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        Query query = db.collection("tasks").orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Error loading tasks", error);
                Toast.makeText(this, "Error loading tasks: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            taskList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Task task = doc.toObject(Task.class);
                    task.setId(doc.getId());

                    if (selectedCategory.equals("All") || task.getCategory().equals(selectedCategory)) {
                        taskList.add(task);
                    }
                }
            }
            taskAdapter.notifyDataSetChanged();
        });
    }

    // Implementation of TaskAdapter.OnTaskActionListener
    @Override
    public void onEditTask(Task task) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_title", task.getTitle());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_date", task.getDate());
        intent.putExtra("task_time", task.getTime());
        intent.putExtra("task_category", task.getCategory());

        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }

    @Override
    public void onDeleteTask(Task task) {
        showDeleteConfirmationDialog(task);
    }

    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTask(task);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteTask(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            Toast.makeText(this, "Error: Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("tasks")
                .document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task deleted successfully");
                    Toasty.success(this, "Task deleted successfully", Toasty.LENGTH_SHORT, true).show();

                    // Remove from local list for immediate UI update
                    taskList.remove(task);
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting task", e);
                    Toasty.success(this, "Failed to delete task", Toasty.LENGTH_SHORT, true).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK) {
            // Task was edited successfully, reload tasks
            loadTasks();
            Toasty.success(this, "Task updated successfully", Toasty.LENGTH_SHORT, true).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
}