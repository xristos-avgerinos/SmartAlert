package com.unipi.chrisavg.smartalert;

import android.location.Location;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class EmergencyAlerts {
    private String Title;
    private long TimeStamp;
    private double Latitude,Longitude;
    private String Category;
    private String Description;

    public EmergencyAlerts() {

    }

    public EmergencyAlerts(String title, long timeStamp, double latitude, double longitude, String category, String description) {
        Title = title;
        TimeStamp = timeStamp;
        Latitude = latitude;
        Longitude = longitude;
        Category = category;
        Description = description;
    }

    public EmergencyAlerts(EmergencyAlerts emergencyAlert) {

        Title = emergencyAlert.getTitle();
        TimeStamp   =  emergencyAlert.getTimeStamp();
        Latitude    =  emergencyAlert.getLatitude();
        Longitude   =  emergencyAlert.getLongitude();
        Category    =  emergencyAlert.getCategory();
        Description =emergencyAlert.getDescription();

    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
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
