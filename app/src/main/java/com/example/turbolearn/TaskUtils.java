package com.example.turbolearn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskUtils {

    public static Map<Task.TaskCategory, Integer> getCompletedTaskCountByCategory(List<Task> taskList) {
        Map<Task.TaskCategory, Integer> completedCounts = new HashMap<>();

        for (Task task : taskList) {
            if (task.isCompleted()) {
                Task.TaskCategory category = task.getCategory();
                completedCounts.put(category, completedCounts.getOrDefault(category, 0) + 1);
            }
        }

        return completedCounts;
    }

    // Method to get silhouette drawable resource for each category
    public static int getCategorySilhouetteResource(Task.TaskCategory category) {
        switch (category) {
            case PERSONAL:
                return R.drawable.ic_person_silhouette;
            case WORK:
                return R.drawable.ic_work_silhouette;
            case STUDY:
                return R.drawable.ic_study_silhouette;
            case HEALTH:
                return R.drawable.ic_health_silhouette;
            case OTHER:
            default:
                return R.drawable.ic_category_other_silhouette;
        }
    }

    // Method to get category background color resource
    public static int getCategoryBackgroundColor(Task.TaskCategory category) {
        switch (category) {
            case PERSONAL:
                return R.color.category_personal;
            case WORK:
                return R.color.category_work;
            case STUDY:
                return R.color.category_study;
            case HEALTH:
                return R.color.category_health;
            case OTHER:
            default:
                return R.color.category_other;
        }
    }

    // Method to get category light background color resource
    public static int getCategoryLightBackgroundColor(Task.TaskCategory category) {
        switch (category) {
            case PERSONAL:
                return R.color.category_personal_light;
            case WORK:
                return R.color.category_work_light;
            case STUDY:
                return R.color.category_study_light;
            case HEALTH:
                return R.color.category_health_light;
            case OTHER:
            default:
                return R.color.category_other_light;
        }
    }
}