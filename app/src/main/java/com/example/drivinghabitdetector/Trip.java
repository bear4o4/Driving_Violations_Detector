package com.example.drivinghabitdetector;

import java.io.Serializable;
import java.util.ArrayList;

public class Trip implements Serializable {
    private String id;
    private String startDate;
    private ArrayList<Violation> violations;

    public Trip(String id, String startDate, ArrayList<Violation> violations) {
        this.id = id;
        this.startDate = startDate;
        this.violations = violations;
    }

    public String getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }
    public ArrayList<Violation> getViolations() {
        return violations;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", startDate='" + startDate + '\'' +
                ", violations=" + violations +
                '}';
    }
}
