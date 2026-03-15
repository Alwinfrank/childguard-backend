package com.childguard.dto;

public class ReverseGeocodeResponse {
    private Double latitude;
    private Double longitude;
    private String displayName;

    public ReverseGeocodeResponse() {
    }

    public ReverseGeocodeResponse(Double latitude, Double longitude, String displayName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
