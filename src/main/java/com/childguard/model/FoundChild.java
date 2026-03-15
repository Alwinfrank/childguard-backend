package com.childguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "found_child")
public class FoundChild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer approxAge;

    private String foundLocation;

    private Double latitude;

    private Double longitude;

    private String photoUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() {
        return id;
    }

    public Integer getApproxAge() {
        return approxAge;
    }

    public void setApproxAge(Integer approxAge) {
        this.approxAge = approxAge;
    }

    public String getFoundLocation() {
        return foundLocation;
    }

    public void setFoundLocation(String foundLocation) {
        this.foundLocation = foundLocation;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
