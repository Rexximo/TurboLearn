package com.example.turbolearn;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private FirebaseFirestore db;
    private LinearLayout cardsContainer;
    private List<Task> allTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        cardsContainer = findViewById(R.id.cards_container);
        db = FirebaseFirestore.getInstance();

        loadTasksFromFirebase();
    }

    private void loadTasksFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allTasks.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Task t = doc.toObject(Task.class);
                                allTasks.add(t);
                            } catch (Exception e) {
                                Log.e(TAG, "Gagal convert task: " + e.getMessage());
                            }
                        }
                        showCompletedTaskCards(allTasks);
                    } else {
                        Log.e(TAG, "Gagal ambil data Firestore", task.getException());
                        Toast.makeText(this, "Gagal mengambil data dari server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCompletedTaskCards(List<Task> tasks) {
        Map<Task.TaskCategory, Integer> completedMap = TaskUtils.getCompletedTaskCountByCategory(tasks);

        // Clear existing cards
        cardsContainer.removeAllViews();

        if (completedMap.isEmpty()) {
            // Show empty state
            showEmptyState();
            return;
        }

        // Create cards in 2-column grid
        LinearLayout currentRow = null;
        int index = 0;

        for (Map.Entry<Task.TaskCategory, Integer> entry : completedMap.entrySet()) {
            // Create new row for every 2 cards
            if (index % 2 == 0) {
                currentRow = createNewRow();
                cardsContainer.addView(currentRow);
            }

            createCategoryCard(currentRow, entry.getKey(), entry.getValue());
            index++;
        }
    }

    private LinearLayout createNewRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dpToPx(2));
        row.setLayoutParams(rowParams);
        return row;
    }

    private void createCategoryCard(LinearLayout parentRow, Task.TaskCategory category, int completedCount) {
        // Inflate card layout
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_completed_task, parentRow, false);

        CardView cardContainer = cardView.findViewById(R.id.card_container);
        TextView categoryNameText = cardView.findViewById(R.id.category_name);
        TextView completedCountText = cardView.findViewById(R.id.completed_count);
        TextView completedLabel = cardView.findViewById(R.id.completed_label);

        // Set content
        categoryNameText.setText(category.getDisplayName());
        completedCountText.setText(String.valueOf(completedCount));

        // Set card color based on category
        int cardColor = getCategoryColor(category);
        cardContainer.setCardBackgroundColor(ContextCompat.getColor(this, cardColor));

        // Set text color for better contrast
        int textColor = getTextColorForBackground(category);
        categoryNameText.setTextColor(ContextCompat.getColor(this, textColor));
        completedCountText.setTextColor(ContextCompat.getColor(this, textColor));
        completedLabel.setTextColor(ContextCompat.getColor(this, textColor));

        // Add margin between cards
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        if (parentRow.getChildCount() > 0) {
            params.setMargins(dpToPx(2), 0, 0, 0);
        }

        // Add to parent row
        parentRow.addView(cardView);
    }

    private int getCategoryColor(Task.TaskCategory category) {
        switch (category) {
            case PERSONAL:
                return R.color.category_personal_light;
            case WORK:
                return R.color.category_work_light;
            case STUDY:
                return R.color.category_study_light;
            case HEALTH:
                return R.color.category_health_light;
            default:
                return R.color.category_other_light;
        }
    }

    private int getTextColorForBackground(Task.TaskCategory category) {
        // Return white text for all colored backgrounds for better contrast
        return android.R.color.white;
    }

    private void showEmptyState() {
        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_completed_tasks, cardsContainer, false);
        cardsContainer.addView(emptyView);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}



