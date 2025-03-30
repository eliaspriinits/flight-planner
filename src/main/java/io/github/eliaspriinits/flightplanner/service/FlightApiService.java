package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import io.github.eliaspriinits.flightplanner.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FlightApiService {
    private static final String API_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private static final String ACCESS_TOKEN = "yNwlquMH7tkXt8QR15tjdtyrzpIn";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;


    @Transactional
    public void fetchAndSaveFlights(String origin, @Nullable String date) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Generate a list of dates if none is provided
        List<String> datesToFetch = new ArrayList<>();
        if (date == null) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 30; i++) { // Fetch flights for the next 30 days
                datesToFetch.add(today.plusDays(i).toString());
            }
        } else {
            datesToFetch.add(date);
        }

        for (String departureDate : datesToFetch) {
            String url = API_URL + "?originLocationCode=" + origin +
                    "&departureDate=" + departureDate + "&adults=1&max=50";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response;

            try {
                response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch flights from Amadeus API", e);
            }

            if (response.getBody() == null || !response.getBody().containsKey("data")) {
                System.err.println("No flights found for date: " + departureDate);
                continue;
            }

            List<Map<String, Object>> flightData = (List<Map<String, Object>>) response.getBody().get("data");

            // Fetch existing flights for the given origin and date
            List<FlightEntity> existingFlights = flightRepository.findByOriginAndDate(origin, LocalDate.parse(departureDate));
            Map<String, FlightEntity> existingFlightMap = existingFlights.stream()
                    .collect(Collectors.toMap(
                            flight -> flight.getOrigin() + flight.getDestination() + flight.getDate().toString(),
                            flight -> flight
                    ));

            Set<String> updatedFlights = new HashSet<>();

            for (Map<String, Object> flightInfo : flightData) {
                try {
                    // Extract destination
                    Map<String, Object> itinerary = ((List<Map<String, Object>>) flightInfo.get("itineraries")).get(0);
                    Map<String, Object> segment = ((List<Map<String, Object>>) itinerary.get("segments")).get(0);
                    String destination = ((Map<String, Object>) segment.get("arrival")).get("iataCode").toString();

                    String flightKey = origin + destination + flightInfo.get("departureTime").toString();

                    if (existingFlightMap.containsKey(flightKey)) {
                        // Update existing flight
                        FlightEntity flight = existingFlightMap.get(flightKey);
                        flight.setPrice(Float.parseFloat(((Map<String, Object>) flightInfo.get("price")).get("total").toString()));
                        flight.setDuration(Float.parseFloat(flightInfo.get("duration").toString()));

                        flightRepository.save(flight);
                    } else {
                        // Create new flight
                        FlightEntity newFlight = new FlightEntity();
                        newFlight.setOrigin(origin);
                        newFlight.setDestination(destination);
                        newFlight.setDate(LocalDateTime.parse(flightInfo.get("departureTime").toString(), FORMATTER));
                        newFlight.setDuration(Float.parseFloat(flightInfo.get("duration").toString()));
                        newFlight.setPrice(Float.parseFloat(((Map<String, Object>) flightInfo.get("price")).get("total").toString()));

                        flightRepository.save(newFlight);
                        generateSeats(newFlight);
                    }
                    updatedFlights.add(flightKey);

                } catch (Exception e) {
                    System.err.println("Error processing flight: " + e.getMessage());
                }
            }

            // Remove flights that are no longer available
            for (FlightEntity existingFlight : existingFlights) {
                String flightKey = existingFlight.getOrigin() + existingFlight.getDestination() + existingFlight.getDate().toString();
                if (!updatedFlights.contains(flightKey)) {
                    flightRepository.delete(existingFlight);
                }
            }
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

