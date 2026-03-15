package com.childguard.service;

import com.childguard.dto.ReverseGeocodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;

    @Value("${geocode.nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String nominatimBaseUrl;

    @Value("${geocode.user-agent:ChildGuard/1.0 (contact:admin@childguard.local)}")
    private String userAgent;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    public ReverseGeocodeResponse reverseGeocode(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);

        URI uri = UriComponentsBuilder
                .fromUriString(nominatimBaseUrl)
                .path("/reverse")
                .queryParam("format", "jsonv2")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("addressdetails", 1)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgent);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
        Map<String, Object> json = response.getBody();

        String displayName = json != null && json.get("display_name") != null
                ? String.valueOf(json.get("display_name"))
                : "";

        return new ReverseGeocodeResponse(latitude, longitude, displayName);
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }
}
