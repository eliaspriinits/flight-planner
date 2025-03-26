package io.github.eliaspriinits.flightplanner.controller;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public List<FlightDto> searchFlights(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) Float duration,
            @RequestParam(required = false) Float price) {
        if (destination == null && date == null && duration == null && price == null) {
            return List.of();
        }
        Map<String, Object> filterMap = new HashMap<>();

        if (destination != null && !destination.isBlank()) {filterMap.put("destination", destination);}
        if (date != null) {filterMap.put("date", date);}
        if (duration != null) {filterMap.put("duration", duration);}
        if (price != null) {filterMap.put("price", price);}

        return flightService.getFlightsByFilters(filterMap);
    }

}
