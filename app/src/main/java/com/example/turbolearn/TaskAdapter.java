package com.example.turbolearn;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private OnTaskActionListener listener;

    // Interface for handling task actions
    public interface OnTaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
        void onToggleTaskCompletion(Task task);
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, Context context, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set basic task information
        holder.textViewTitle.setText(task.getTitle());
        holder.textViewDescription.setText(task.getDescription());
        holder.textViewDateTime.setText(task.getFormattedDueDate());

        // Set category display name using the enum
        String categoryText = task.getCategory() != null ?
                task.getCategory().getDisplayName() : "Other";
        holder.textViewCategory.setText(categoryText);

        // Set priority if TextView exists
        if (holder.textViewPriority != null) {
            String priorityText = task.getPriority() != null ?
                    task.getPriority().getDisplayName() : "Medium";
            holder.textViewPriority.setText(priorityText);
        }

        // Handle task completion state
        handleTaskCompletionState(holder, task);

        // Set category colors and background
        setCategoryColors(holder, task);

        // Set priority colors if priority TextView exists
        if (holder.textViewPriority != null) {
            setPriorityColors(holder, task);
        }

        // Set overdue indicator
        setOverdueIndicator(holder, task);

        // Set click listeners
        setClickListeners(holder, task);
    }

    private void handleTaskCompletionState(TaskViewHolder holder, Task task) {
        if (task.isCompleted()) {
            // Strike through text for completed tasks
            holder.textViewTitle.setPaintFlags(
                    holder.textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewDescription.setPaintFlags(
                    holder.textViewDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // Reduce opacity
            holder.cardView.setAlpha(0.6f);

            // Change card background for completed tasks
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.completed_task_background));
        } else {
            // Remove strike through
            holder.textViewTitle.setPaintFlags(
                    holder.textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewDescription.setPaintFlags(
                    holder.textViewDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            // Reset opacity
            holder.cardView.setAlpha(1.0f);
        }
    }

    private void setCategoryColors(TaskViewHolder holder, Task task) {
        if (task.getCategory() == null) {
            holder.textViewCategory.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.category_other));
            holder.textViewCategory.setTextColor(
                    ContextCompat.getColor(context, android.R.color.white));
            return;
        }

        int backgroundColor;
        int cardBackgroundColor;

        // Use the enum values instead of string comparison
        switch (task.getCategory()) {
            case PERSONAL:
                backgroundColor = R.color.category_personal;
                cardBackgroundColor = R.color.category_personal_light;
                break;
            case WORK:
                backgroundColor = R.color.category_work;
                cardBackgroundColor = R.color.category_work_light;
                break;
            case STUDY:
                backgroundColor = R.color.category_study;
                cardBackgroundColor = R.color.category_study_light;
                break;
            case HEALTH:
                backgroundColor = R.color.category_health;
                cardBackgroundColor = R.color.category_health_light;
                break;
            case OTHER:
            default:
                backgroundColor = R.color.category_other;
                cardBackgroundColor = R.color.category_other_light;
                break;
        }

        holder.textViewCategory.setBackgroundColor(
                ContextCompat.getColor(context, backgroundColor));
        holder.textViewCategory.setTextColor(
                ContextCompat.getColor(context, android.R.color.white));

        // Set card background only if task is not completed
        if (!task.isCompleted()) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, cardBackgroundColor));
        }
    }

    private void setPriorityColors(TaskViewHolder holder, Task task) {
        if (task.getPriority() == null) {
            holder.textViewPriority.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.priority_medium));
            return;
        }

        int priorityColor;
        switch (task.getPriority()) {
            case LOW:
                priorityColor = R.color.priority_low;
                break;
            case MEDIUM:
                priorityColor = R.color.priority_medium;
                break;
            case HIGH:
                priorityColor = R.color.priority_high;
                break;
            case URGENT:
                priorityColor = R.color.priority_urgent;
                break;
            default:
                priorityColor = R.color.priority_medium;
                break;
        }

        holder.textViewPriority.setBackgroundColor(
                ContextCompat.getColor(context, priorityColor));
        holder.textViewPriority.setTextColor(
                ContextCompat.getColor(context, android.R.color.white));
    }

    private void setOverdueIndicator(TaskViewHolder holder, Task task) {
        if (holder.textViewOverdue != null) {
            if (task.isOverdue()) {
                holder.textViewOverdue.setVisibility(View.VISIBLE);
                holder.textViewOverdue.setText("OVERDUE");
                holder.textViewOverdue.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.primary_color));
                holder.textViewOverdue.setTextColor(
                        ContextCompat.getColor(context, android.R.color.white));
            } else {
                holder.textViewOverdue.setVisibility(View.GONE);
            }
        }
    }

    private void setClickListeners(TaskViewHolder holder, Task task) {
        // Edit button
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTask(task);
            }
        });

        // Delete button
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTask(task);
            }
        });

        // Toggle completion button (if exists)
        if (holder.buttonToggleComplete != null) {
            // Set button icon based on completion state
            int iconRes = task.isCompleted() ?
                    R.drawable.ic_check_circle : R.drawable.ic_circle;
            holder.buttonToggleComplete.setImageResource(iconRes);

            holder.buttonToggleComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleTaskCompletion(task);
                }
            });
        }

        // Entire card click
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        // Long click for quick actions
        holder.cardView.setOnLongClickListener(v -> {
            // Show context menu or quick actions
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    // Method to update task list
    public void updateTaskList(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    // Method to add single task
    public void addTask(Task task) {
        if (taskList != null) {
            taskList.add(task);
            notifyItemInserted(taskList.size() - 1);
        }
    }

    // Method to remove task
    public void removeTask(int position) {
        if (taskList != null && position >= 0 && position < taskList.size()) {
            taskList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to update single task
    public void updateTask(int position, Task task) {
        if (taskList != null && position >= 0 && position < taskList.size()) {
            taskList.set(position, task);
            notifyItemChanged(position);
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewDateTime;
        TextView textViewCategory;
        TextView textViewPriority;  // Optional - add to layout if needed
        TextView textViewOverdue;   // Optional - add to layout if needed
        ImageButton buttonEdit;
        ImageButton buttonDelete;
        ImageButton buttonToggleComplete;  // Optional - add to layout if needed

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewTask);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // Optional views - will be null if not in layout
            textViewPriority = itemView.findViewById(R.id.textViewPriority);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);
            buttonToggleComplete = itemView.findViewById(R.id.buttonToggleComplete);
        }
    }
}