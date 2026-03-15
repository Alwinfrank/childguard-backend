package com.childguard.controller;

import com.childguard.model.FoundChild;
import com.childguard.service.FoundChildService;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/found")
@CrossOrigin
public class FoundChildController {

    private final FoundChildService service;

    public FoundChildController(FoundChildService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public FoundChild save(@RequestBody FoundChild child) {
        return service.save(child);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FoundChild saveMultipart(
            @RequestParam Integer approxAge,
            @RequestParam String foundLocation,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String photoUrl,
            @RequestPart(required = false) MultipartFile photo
    ) throws IOException {
        FoundChild child = new FoundChild();
        child.setApproxAge(approxAge);
        child.setFoundLocation(foundLocation);
        child.setLatitude(latitude);
        child.setLongitude(longitude);

        if (photo != null && !photo.isEmpty()) {
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String originalName = photo.getOriginalFilename() == null ? "found.jpg" : photo.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path targetPath = uploadDir.resolve(fileName);
            photo.transferTo(targetPath.toFile());
            String photoPublicUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            child.setPhotoUrl(photoPublicUrl);
        } else {
            child.setPhotoUrl(photoUrl);
        }

        return service.save(child);
    }

    @GetMapping
    public List<FoundChild> getAll() {
        return service.getAll();
    }
}
