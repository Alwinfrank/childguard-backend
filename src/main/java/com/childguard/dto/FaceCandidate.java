package com.childguard.dto;

public class FaceCandidate {
    private Long id;
    private Integer age;
    private Double latitude;
    private Double longitude;
    private String photoUrl;

    public FaceCandidate() {
    }

    public FaceCandidate(Long id, Integer age, Double latitude, Double longitude, String photoUrl) {
        this.id = id;
        this.age = age;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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
}
