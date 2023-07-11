package com.fullsail.parkfinder;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class cPlace implements Serializable {
    private String fullName;
    private String url;
    private String description;
    private double lat;
    private double lng;
    private List<String> images;
    private List<String> amenities;
    private List<String> fees;
    private String type;

    public cPlace(String fullName, String url, String description, double lat, double lng, List<String> images, List<String> amenities, List<String> fees) {
        this.fullName = fullName;
        this.url = url;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.images = images;
        this.amenities = amenities;
        this.fees = fees;
    }

    public cPlace() {

    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public void setFees(List<String> fees) {
        this.fees = fees;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public List<String> getFees() {
        return fees;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getType() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return fullName;
    }
}
