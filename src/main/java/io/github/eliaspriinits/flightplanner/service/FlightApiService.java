package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import io.github.eliaspriinits.flightplanner.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class FlightApiService {
    private static final String API_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private static final String ACCESS_TOKEN = "YOUR_AMADEUS_ACCESS_TOKEN";

    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    public void fetchAndSaveFlights(String origin, String destination, String date) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = API_URL + "?originLocationCode=" + origin +
                "&destinationLocationCode=" + destination +
                "&departureDate=" + date + "&adults=1&max=10";

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> flightData = (List<Map<String, Object>>) response.getBody().get("data");

        for (Map<String, Object> flightInfo : flightData) {
            FlightEntity flight = new FlightEntity();
            flight.setDestination(destination);
            flight.setDate(LocalDateTime.parse(flightInfo.get("departureTime").toString()));
            flight.setDuration(Float.parseFloat(flightInfo.get("duration").toString()));
            flight.setPrice(Float.parseFloat(((Map<String, Object>) flightInfo.get("price")).get("total").toString()));

            flightRepository.save(flight);
            generateSeats(flight);
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

