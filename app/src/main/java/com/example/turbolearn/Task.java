package com.example.turbolearn;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

public class Task {
    public enum TaskCategory {
        PERSONAL("Personal"),
        WORK("Work"),
        STUDY("Study"),
        HEALTH("Health"),
        OTHER("Other");

        private final String displayName;

        TaskCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String id;
    private String userId; // Tambahan: ID pengguna pemilik task
    private String title;
    private String description;
    private Date dueDate;
    private TaskCategory category;
    private Priority priority;
    private boolean isCompleted;
    private long timestamp;
    private Date createdAt;
    private Date updatedAt;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Constructor kosong untuk Firebase
    public Task() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor utama dengan userId
    public Task(String userId, String title, String description, Date dueDate, TaskCategory category) {
        this();
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        setTitle(title);
        setDescription(description);
        this.dueDate = dueDate;
        this.category = category != null ? category : TaskCategory.OTHER;
        this.priority = Priority.MEDIUM;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor lengkap dengan priority
    public Task(String userId, String title, String description, Date dueDate, TaskCategory category, Priority priority) {
        this(userId, title, description, dueDate, category);
        this.priority = priority != null ? priority : Priority.MEDIUM;
    }

    // Constructor kompatibilitas lama
    public Task(String title, String description, String date, String time, String category) {
        this();
        this.id = UUID.randomUUID().toString();
        setTitle(title);
        setDescription(description);
        setDateTimeFromStrings(date, time);
        setCategoryFromString(category);
        this.priority = Priority.MEDIUM;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
        updateTimestamp();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
        updateTimestamp();
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        updateTimestamp();
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category != null ? category : TaskCategory.OTHER;
        updateTimestamp();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
        updateTimestamp();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        updateTimestamp();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utilities
    public boolean isOverdue() {
        if (dueDate == null || isCompleted) {
            return false;
        }
        return new Date().after(dueDate);
    }

    public String getFormattedDueDate() {
        return dueDate != null ? DATE_FORMAT.format(dueDate) : "No due date";
    }

    public long getDaysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        long diff = dueDate.getTime() - System.currentTimeMillis();
        return diff / (24 * 60 * 60 * 1000);
    }

    public void markAsCompleted() {
        setCompleted(true);
    }

    public void markAsIncomplete() {
        setCompleted(false);
    }

    public String getDate() {
        return dueDate != null ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDate) : "";
    }

    public String getTime() {
        return dueDate != null ? new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueDate) : "";
    }

    public void setDate(String date) {
        updateDateTimeFromStrings(date, getTime());
    }

    public void setTime(String time) {
        updateDateTimeFromStrings(getDate(), time);
    }

    public void setCategoryFromString(String categoryStr) {
        if (categoryStr == null || categoryStr.trim().isEmpty()) {
            this.category = TaskCategory.OTHER;
            return;
        }

        try {
            this.category = TaskCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.category = TaskCategory.OTHER;
        }
    }

    public String getCategoryAsString() {
        return category != null ? category.name() : TaskCategory.OTHER.name();
    }

    private void updateTimestamp() {
        this.updatedAt = new Date();
        this.timestamp = System.currentTimeMillis();
    }

    private void setDateTimeFromStrings(String date, String time) {
        if (date == null || date.trim().isEmpty()) {
            this.dueDate = null;
            return;
        }

        try {
            String dateTime = date + " " + (time != null ? time : "00:00");
            this.dueDate = DATE_FORMAT.parse(dateTime);
        } catch (ParseException e) {
            this.dueDate = null;
        }
    }

    private void updateDateTimeFromStrings(String date, String time) {
        setDateTimeFromStrings(date, time);
        updateTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) && Objects.equals(userId, task.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + getFormattedDueDate() +
                ", category=" + (category != null ? category.getDisplayName() : "None") +
                ", priority=" + (priority != null ? priority.getDisplayName() : "None") +
                ", isCompleted=" + isCompleted +
                ", isOverdue=" + isOverdue() +
                '}';
    }
}
