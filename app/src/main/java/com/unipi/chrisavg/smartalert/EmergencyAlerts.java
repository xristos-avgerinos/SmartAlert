package com.unipi.chrisavg.smartalert;

import java.io.Serializable;

public class EmergencyAlerts implements Serializable {
    private String key;
    private String Title;
    private long TimeStamp;
    private double Latitude,Longitude;
    private String Address;
    private String Category;
    private String Description;
    private String status;

    public EmergencyAlerts() {

    }

    public EmergencyAlerts(String title, long timeStamp, double latitude, double longitude,String address, String category, String description) {
        Title = title;
        TimeStamp = timeStamp;
        Latitude = latitude;
        Longitude = longitude;
        Address=address;
        Category = category;
        Description = description;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        this.Address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
