package io.github.eliaspriinits.flightplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import io.github.eliaspriinits.flightplanner.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FlightApiService {
    private static final String API_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private String accessToken = null;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    @Value("${amadeus.client-id}")
    private String amadeusClientId;

    @Value("${amadeus.client-secret}")
    private String amadeusClientSecret;
    private Logger logger = Logger.getLogger(FlightApiService.class.getName());


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
    @Transactional
    public void fetchAndSaveFlights(String origin, @Nullable String date, @Nullable String destination) {
        int count = 0;
        if (accessToken == null) {
            authenticate();
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<String> datesToFetch = new ArrayList<>();
        if (date == null) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 30; i++) { // fetch flights for the next 30 days
                datesToFetch.add(today.plusDays(i).toString());
            }
        } else {
            datesToFetch.add(date);
        }
        List<String> destinations = new ArrayList<>();
        if (destination != null) {
            destinations.add(destination);
        } else {
            getDestinationsFromCSV("airports.csv", destinations);
        }

        for (String destinationElement : destinations) {
            for (String departureDate : datesToFetch) {
                String url = API_URL + "?originLocationCode=" + origin +
                        "&destinationLocationCode=" + destinationElement +
                        "&departureDate=" + departureDate.toString() + "&adults=1&max=50";
                System.out.println(departureDate);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response;

                try {
                    response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch flights from Amadeus API", e);
                }

                if (response.getBody() == null || !response.getBody().containsKey("data")) {
                    System.err.println("No flights found for date: " + departureDate + " to " + destinationElement);
                    continue;
                }

                List<Map<String, Object>> flightData = (List<Map<String, Object>>) response.getBody().get("data");

                List<FlightEntity> existingFlights = flightRepository.findByOriginAndDestinationAndDate(origin, destinationElement, LocalDate.parse(departureDate).atStartOfDay());
                Map<String, FlightEntity> existingFlightMap = existingFlights.stream()
                        .collect(Collectors.toMap(
                                flight -> flight.getOrigin() + flight.getDestination() + flight.getDate().toString(),
                                flight -> flight
                        ));

                Set<String> updatedFlights = new HashSet<>();

                for (Map<String, Object> flightInfo : flightData) {
                    System.out.println(flightInfo);
                    try {
                        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) flightInfo.get("itineraries");
                        if (itineraries == null || itineraries.isEmpty()) {
                            System.err.println("Missing itineraries for flight: " + flightInfo);
                            continue;
                        }

                        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments");
                        if (segments == null || segments.isEmpty()) {
                            System.err.println("Missing segments for flight: " + flightInfo);
                            continue;
                        }

                        Map<String, Object> segment = segments.get(0);
                        Map<String, Object> arrival = (Map<String, Object>) segment.get("arrival");
                        if (arrival == null || arrival.get("iataCode") == null) {
                            System.err.println("Missing arrival IATA code for flight: " + flightInfo);
                            continue;
                        }
                        String flightDestination = arrival.get("iataCode").toString();

                        Map<String, Object> departure = (Map<String, Object>) segment.get("departure");
                        if (departure == null || departure.get("at") == null) {
                            System.err.println("Missing departure time for flight: " + flightInfo);
                            continue;
                        }
                        String departureTime = departure.get("at").toString();

                        String flightKey = origin + flightDestination + departureTime;

                        if (existingFlightMap.containsKey(flightKey)) {
                            FlightEntity flight = existingFlightMap.get(flightKey);
                            String price = ((Map<String, Object>) flightInfo.get("price")).get("total") != null ? ((Map<String, Object>) flightInfo.get("price")).get("total").toString() : null;
                            if (price != null) {
                                flight.setPrice(Float.parseFloat(price));
                            }
                            flight.setDuration(Float.parseFloat(flightInfo.get("duration") != null ? flightInfo.get("duration").toString() : "0"));

                            flightRepository.save(flight);
                        } else {
                            FlightEntity newFlight = new FlightEntity();
                            newFlight.setOrigin(origin);
                            newFlight.setDestination(flightDestination);
                            newFlight.setDate(LocalDateTime.parse(departureTime, FORMATTER));
                            newFlight.setDuration(Float.parseFloat(flightInfo.get("duration") != null ? flightInfo.get("duration").toString() : "0"));

                            String price = ((Map<String, Object>) flightInfo.get("price")).get("total") != null ? ((Map<String, Object>) flightInfo.get("price")).get("total").toString() : null;
                            if (price != null) {
                                newFlight.setPrice(Float.parseFloat(price));
                            }

                            flightRepository.save(newFlight);
                            generateSeats(newFlight);
                        }
                        updatedFlights.add(flightKey);

                    } catch (Exception e) {
                        System.err.println("Error processing flight: " + e.getMessage());
                    }
                }
                for (FlightEntity existingFlight : existingFlights) {
                    String flightKey = existingFlight.getOrigin() + existingFlight.getDestination() + existingFlight.getDate().toString();
                    if (!updatedFlights.contains(flightKey)) {
                        flightRepository.delete(existingFlight);
                    }
                }
            }
        }
    }
    private void getDestinationsFromCSV(String filePath, List<String> destinations) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] nextLine;
            reader.readNext(); // skips the header line

            while ((nextLine = reader.readNext()) != null) {
                try {
                    String code = nextLine[2];
                    destinations.add(code);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Skipping invalid row: " + String.join(", ", nextLine));
                }
            }
        } catch (IOException | CsvValidationException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }
    private void generateSeats(FlightEntity flight) {
        List<SeatEntity> seats = new ArrayList<>();
        for (int i = 1; i <= 30; i++) { // Assuming 30 seats per flight
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setStatus(SeatEntity.SeatStatus.AVAILABLE);
            seat.setFlight(flight);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }
}

