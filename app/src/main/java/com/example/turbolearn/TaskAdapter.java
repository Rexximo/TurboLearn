package com.example.turbolearn;

import android.content.Context;
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

        holder.textViewTitle.setText(task.getTitle());
        holder.textViewDescription.setText(task.getDescription());
        holder.textViewDateTime.setText(task.getDate() + " " + task.getTime());
        holder.textViewCategory.setText(task.getCategory());

        // Set category color and background
        if (task.getCategory().equals("Easy")) {
            holder.textViewCategory.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.easy_color));
            holder.textViewCategory.setTextColor(
                    ContextCompat.getColor(context, android.R.color.white));
        } else if (task.getCategory().equals("Hard")) {
            holder.textViewCategory.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.hard_color));
            holder.textViewCategory.setTextColor(
                    ContextCompat.getColor(context, android.R.color.white));
        }

        // Set card background based on category
        if (task.getCategory().equals("Easy")) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.easy_light));
        } else if (task.getCategory().equals("Hard")) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.hard_light));
        } else {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.background_primary));
        }

        // Set click listeners for edit and delete buttons
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTask(task);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTask(task);
            }
        });

        // Optional: Set click listener for entire card
        holder.cardView.setOnClickListener(v -> {
            // You can add click action for the entire card here
            // For example, show task details
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewDateTime;
        TextView textViewCategory;
        ImageButton buttonEdit;
        ImageButton buttonDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewTask);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}