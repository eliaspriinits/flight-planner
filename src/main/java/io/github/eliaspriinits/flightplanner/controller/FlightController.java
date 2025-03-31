package io.github.eliaspriinits.flightplanner.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final static Logger logger = Logger.getLogger(FlightController.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${amadeus.client-id}")
    private String amadeusClientId;

    @Value("${amadeus.client-secret}")
    private String amadeusClientSecret;

    private String accessToken = null;

    // Fetch Amadeus API Access Token
    private void authenticate() {
        try {
            String url = "https://test.api.amadeus.com/v1/security/oauth2/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", amadeusClientId);
            body.add("client_secret", amadeusClientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);

            if (response.getBody() != null && response.getBody().has("access_token")) {
                accessToken = response.getBody().get("access_token").asText();
                logger.info("Amadeus API token acquired.");
            }
        } catch (Exception e) {
            logger.severe("Error fetching Amadeus API token: " + e.getMessage());
        }
    }

    // Get IATA Code for a City
    @GetMapping("/iata/{cityName}")
    public ResponseEntity<String> getIATACode(@PathVariable String cityName) {
        if (accessToken == null) {
            authenticate(); // Get API token if not available
        }

        try {
            String url = "https://test.api.amadeus.com/v1/reference-data/locations?keyword=" + cityName + "&subType=AIRPORT";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            // Log full response for debugging
            logger.info("Amadeus API Response: " + response.getBody());

            if (response.getBody() != null && response.getBody().has("data")) {
                JsonNode data = response.getBody().get("data");
                if (data.isArray() && data.size() > 0 && data.get(0).has("iataCode")) {
                    return ResponseEntity.ok(data.get(0).get("iataCode").asText());
                }
            }

            logger.warning("No valid IATA code found for: " + cityName);
        } catch (Exception e) {
            logger.severe("Error fetching IATA code: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid city name or API error");
    }

    // Search for Flights
    @GetMapping("/search")
    public ResponseEntity<List<FlightDto>> searchFlights(
            @RequestParam String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) Float minDuration,
            @RequestParam(required = false) Float maxPrice) {

        if (origin.isBlank()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Convert city name to IATA code
        ResponseEntity<String> responseOrigin = getIATACode(origin);
        ResponseEntity<String> responseDestination = getIATACode(origin);
        if (!responseOrigin.getStatusCode().is2xxSuccessful() || responseOrigin.getBody() == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        String iataCode = responseOrigin.getBody();


        flightService.fetchFlights(iataCode, date != null ? date.toString() : null, null);
        logger.info("Searching flights from " + iataCode);

        return ResponseEntity.ok(flightService.filterFlights(destination, date, minDuration, maxPrice));
    }

    // Book a seat
    @PostMapping("/{flightId}/book")
    public ResponseEntity<FlightDto> bookSeat(@PathVariable Long flightId, @RequestParam String seatNumber) {
        return ResponseEntity.ok(flightService.bookSeat(flightId, seatNumber));
    }
}


