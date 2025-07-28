package com.example.turbolearn;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
    private TextView tvWelcome; // TextView untuk menampilkan nama user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        cardsContainer = findViewById(R.id.cards_container);
        tvWelcome = findViewById(R.id.tvWelcome); // Pastikan ID ini ada di layout
        db = FirebaseFirestore.getInstance();

        // Load user data first
        loadUserData();
        loadTasksFromFirebase();
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            getUserData(userId);
        } else {
            // Set default welcome message if no user is logged in
            if (tvWelcome != null) {
                tvWelcome.setText("Selamat datang!");
            }
        }
    }

    private void getUserData(String userId) {
        try {
            Log.d(TAG, "Getting user data for UID: " + userId);

            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Ambil data nama dari dokumen
                                    String userName = document.getString("name");
                                    if (userName != null && !userName.isEmpty() && tvWelcome != null) {
                                        // Tampilkan nama di TextView
                                        tvWelcome.setText(userName);
                                        Log.d(TAG, "User name loaded: " + userName);
                                    } else {
                                        if (tvWelcome != null) {
                                            tvWelcome.setText("Selamat datang!");
                                        }
                                        Log.w(TAG, "User name is null or empty");
                                    }
                                } else {
                                    Log.w(TAG, "User document does not exist");
                                    if (tvWelcome != null) {
                                        tvWelcome.setText("Selamat datang!");
                                    }
                                }
                            } else {
                                Log.e(TAG, "Error getting user data", task.getException());
                                if (tvWelcome != null) {
                                    tvWelcome.setText("Selamat datang!");
                                }
                                // Don't show error toast for user data loading failure
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing user data", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserData", e);
        }
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
        ImageView categorySilhouette = cardView.findViewById(R.id.imageViewCategorySilhouette);

        // Set content
        categoryNameText.setText(category.getDisplayName());
        completedCountText.setText(String.valueOf(completedCount));

        // Set card color based on category
        int cardColor = getCategoryColor(category);
        cardContainer.setCardBackgroundColor(ContextCompat.getColor(this, cardColor));

        // Set category silhouette
        setCategorySilhouette(categorySilhouette, category);

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

    private void setCategorySilhouette(ImageView silhouetteView, Task.TaskCategory category) {
        if (silhouetteView == null) return;

        int silhouetteDrawable;

        // Set silhouette drawable based on category (same logic as TaskAdapter)
        switch (category) {
            case PERSONAL:
                silhouetteDrawable = R.drawable.ic_person_silhouette;
                break;
            case WORK:
                silhouetteDrawable = R.drawable.ic_work_silhouette;
                break;
            case STUDY:
                silhouetteDrawable = R.drawable.ic_study_silhouette;
                break;
            case HEALTH:
                silhouetteDrawable = R.drawable.ic_health_silhouette;
                break;
            case OTHER:
            default:
                silhouetteDrawable = R.drawable.ic_category_other_silhouette;
                break;
        }

        silhouetteView.setImageResource(silhouetteDrawable);

        // Set appropriate alpha for background silhouette
        silhouetteView.setAlpha(0.15f);
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