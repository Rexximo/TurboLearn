package com.example.turbolearn;

public class Task {
    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String category;
    private boolean isCompleted;
    private long timestamp;

    public Task() {
        // Required empty constructor for Firestore
    }

    public Task(String title, String description, String date, String time, String category) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.category = category;
        this.isCompleted = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
