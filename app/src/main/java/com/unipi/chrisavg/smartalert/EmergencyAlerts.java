package com.unipi.chrisavg.smartalert;

import java.time.LocalDateTime;

public class EmergencyAlerts {
    private String Title;
    private LocalDateTime TimeStamp;
    private String Location;
    private String Category;
    private String Description;

    public EmergencyAlerts(String title, LocalDateTime timeStamp, String location, String category, String description) {
        Title = title;
        TimeStamp = timeStamp;
        Location = location;
        Category = category;
        Description = description;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public LocalDateTime getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
