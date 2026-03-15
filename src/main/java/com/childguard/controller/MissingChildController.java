package com.childguard.controller;

import com.childguard.model.MissingChild;
import com.childguard.service.MissingChildService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missing")
@CrossOrigin(origins = "*")
public class MissingChildController {

    private final MissingChildService service;

    public MissingChildController(MissingChildService service) {
        this.service = service;
    }

    @PostMapping
    public MissingChild addMissingChild(
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam String lastSeenLocation,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam MultipartFile photo,
            @RequestParam(required = false) String parentName,
            @RequestParam(required = false) String parentPhone,
            @RequestParam(required = false) String parentIdType,
            @RequestParam(required = false) String parentIdNumber,
            @RequestParam(required = false) String parentIdProof
    ) throws IOException {

        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String originalName = photo.getOriginalFilename() == null ? "photo.jpg" : photo.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(fileName);
        photo.transferTo(targetPath.toFile());
        String photoPublicUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        MissingChild child = new MissingChild();
        child.setName(name);
        child.setAge(age);
        child.setLastSeenLocation(lastSeenLocation);
        child.setLatitude(latitude);
        child.setLongitude(longitude);
        child.setPhotoUrl(photoPublicUrl);
        child.setParentName(parentName);
        child.setParentPhone(parentPhone);
        child.setParentIdType(parentIdType);
        child.setParentIdNumber(parentIdNumber);
        child.setParentIdProof(parentIdProof);

        return service.add(child);
    }

    @GetMapping
    public List<MissingChild> getAll() {
        return service.getAll();
    }
}
