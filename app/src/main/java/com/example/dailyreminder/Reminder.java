package com.example.dailyreminder;

import java.io.Serializable;

public class Reminder implements Serializable {
    private int id;  // Unique identifier for each reminder
    private String title;
    private String description;
    private String time;
    private boolean completed;

    public Reminder(int id, String title, String description, String time, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.time = time;
        this.completed = false;
    }

    // Getter and setter for the ID field
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", completed=" + completed +
                '}';
    }
}