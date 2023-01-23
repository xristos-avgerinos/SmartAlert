package com.unipi.chrisavg.smartalert;

import android.location.Location;
import android.location.LocationListener;

public class Users {
    private String fullname;
    private String phoneNumber;
    private Location location;
    private String role;

    public Users(String fullname, String phoneNumber,String role) {
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Users() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
