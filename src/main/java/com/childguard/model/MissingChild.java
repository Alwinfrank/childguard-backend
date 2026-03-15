package com.childguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MissingChild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= CHILD INFO =================
    private String name;
    private int age;
    private String lastSeenLocation;
    private Double latitude;
    private Double longitude;
    private String photoUrl;

    private LocalDateTime missingDate = LocalDateTime.now();
    private String status = "MISSING";

    // ================= PARENT INFO (NEW) =================
    private String parentName;
    private String parentPhone;
    private String parentIdType;
    private String parentIdNumber;
    private String parentIdProof;

    // ================= GETTERS & SETTERS =================

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLastSeenLocation() { return lastSeenLocation; }
    public void setLastSeenLocation(String lastSeenLocation) { this.lastSeenLocation = lastSeenLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public LocalDateTime getMissingDate() { return missingDate; }
    public void setMissingDate(LocalDateTime missingDate) { this.missingDate = missingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getParentIdType() { return parentIdType; }
    public void setParentIdType(String parentIdType) { this.parentIdType = parentIdType; }

    public String getParentIdNumber() { return parentIdNumber; }
    public void setParentIdNumber(String parentIdNumber) { this.parentIdNumber = parentIdNumber; }

    public String getParentIdProof() { return parentIdProof; }
    public void setParentIdProof(String parentIdProof) { this.parentIdProof = parentIdProof; }
}
