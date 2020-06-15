package com.example.androidassignment.entity;

import com.google.android.gms.maps.model.LatLng;

public class MarkerItem {
    private int markerId;
    private String markerTitle;
    private double lat;
    private double lng;
    private byte[] markerImage;

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

    public byte[] getMarkerImage() {
        return markerImage;
    }

    public MarkerItem(int markerId, String markerTitle, double lat, double lng, byte[] markerImage) {
        this.markerId = markerId;
        this.markerTitle = markerTitle;
        this.lat = lat;
        this.lng = lng;
        this.markerImage = markerImage;
    }

    public MarkerItem(String markerTitle, double lat, double lng) {
        this.markerTitle = markerTitle;
        this.lat = lat;
        this.lng = lng;
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

    public void setMarkerImage(byte[] markerImage) {
        this.markerImage = markerImage;
    }
}
