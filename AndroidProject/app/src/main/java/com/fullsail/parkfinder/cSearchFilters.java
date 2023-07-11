package com.fullsail.parkfinder;

import java.io.Serializable;

public class cSearchFilters implements Serializable {
    private String zipCode;
    private int radius;
    private String designation;
    private int displayCount;

    public cSearchFilters() {
    }

    public cSearchFilters(String zipCode, int radius, String designation, int displayCount) {
        this.zipCode = zipCode;
        this.radius = radius;
        this.designation = designation;
        this.displayCount = displayCount;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
    }

    public String getZipCode() {
        return zipCode;
    }

    public int getRadius() {
        return radius;
    }

    public String getDesignation() {
        return designation;
    }

    public int getDisplayCount() {
        return displayCount;
    }
}
