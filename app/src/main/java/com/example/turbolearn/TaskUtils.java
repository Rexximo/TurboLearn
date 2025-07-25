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

    // Optional: Method to display the results in console/log
    public static void printCompletedTaskByCategory(List<Task> taskList) {
        Map<Task.TaskCategory, Integer> completedMap = getCompletedTaskCountByCategory(taskList);
        for (Map.Entry<Task.TaskCategory, Integer> entry : completedMap.entrySet()) {
            System.out.println(entry.getKey().getDisplayName() + ": " + entry.getValue() + " task selesai");
        }
    }
}
