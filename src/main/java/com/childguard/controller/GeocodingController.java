package com.childguard.controller;

import com.childguard.dto.ReverseGeocodeResponse;
import com.childguard.service.GeocodingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/geocode")
@CrossOrigin(origins = "*")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/reverse")
    public ResponseEntity<?> reverse(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude
    ) {
        try {
            ReverseGeocodeResponse response = geocodingService.reverseGeocode(latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Failed to resolve address from coordinates."));
        }
    }
}
