package com.example.drivinghabitdetector;

import java.io.Serializable;

public class Violation implements Serializable {
    private double latitude;
    private double longitude;
    private String type;
    private String dateTime;

    public Violation(double latitude, double longitude, String type, String dateTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.dateTime = dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getDateTime() {
        return dateTime;
    }
}
