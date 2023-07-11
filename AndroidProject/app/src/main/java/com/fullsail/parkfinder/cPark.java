package com.fullsail.parkfinder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class cPark extends cPlace implements Serializable {
    private String parkCode;
    private List<String> activities;
    private List<String> states;
    private cContactInformation contactInformation;
    private String designation;

    public cPark() {
    }

    public cPark(String parkCode, List<String> activities, List<String> states, cContactInformation contactInformation, String designation) {
        this.parkCode = parkCode;
        this.activities = activities;
        this.states = states;
        this.contactInformation = contactInformation;
        this.designation = designation;
    }

    public void setParkCode(String parkCode) {
        this.parkCode = parkCode;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public void setStates(String states) {
        this.states = Arrays.asList(states.split("[,]", 0));
    }

    public void setContactInformation(cContactInformation contactInformation) {
        this.contactInformation = contactInformation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getParkCode() {
        return parkCode;
    }

    public List<String> getActivities() {
        return activities;
    }

    public List<String> getStates() {
        return states;
    }

    public cContactInformation getContactInformation() {
        return contactInformation;
    }

    public String getDesignation() {
        return designation;
    }
}
