package com.example.androidassignment.entity;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class MarkerItem {
    private int markerId;
    private String markerTitle;
    private String description;
    private double lat;
    private double lng;
    private String markerImage;

    public MarkerItem(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    public int getMarkerId() {
        return markerId;
    }

    public String getMarkerTitle() {
        return markerTitle;
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    public String getMarkerImage() {
        return markerImage;
    }

    public MarkerItem(int markerId, String markerTitle, String description, double lat, double lng, String markerImage) {
        this.markerId = markerId;
        this.markerTitle = markerTitle;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.markerImage = markerImage;
    }

    public MarkerItem(String markerTitle, String description, double lat, double lng) {
        this.markerTitle = markerTitle;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMarkerId(int markerId) {
        this.markerId = markerId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setMarkerImage(String markerImage) {
        this.markerImage = markerImage;
    }

    @NonNull
    @Override
    public String toString() {
        return this.markerTitle;
    }
}
