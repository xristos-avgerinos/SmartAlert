package com.unipi.chrisavg.smartalert;

import android.location.Location;
import android.location.LocationListener;

public class Users {
    private String fullname;
    private String phoneNumber;
    private double Latitude,Longitude;
    private String role;

    public Users(String fullname, String phoneNumber,String role) {
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public Users() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
