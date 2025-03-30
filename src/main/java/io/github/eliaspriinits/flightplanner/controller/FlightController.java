package io.github.eliaspriinits.flightplanner.controller;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    public ResponseEntity<List<FlightDto>> searchFlights(
            String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) Float minDuration,
            @RequestParam(required = false) Float maxPrice) {

        if (origin == null) {
            return ResponseEntity.noContent().build();
        }
        flightService.fetchFlights(origin, date.toString());
        logger.info("Searching flights");
        logger.info(flightService.filterFlights(destination, date, minDuration, maxPrice).toString());
        return ResponseEntity.ok(flightService.filterFlights(destination, date, minDuration, maxPrice));
    }
    @PostMapping("/{flightId}/book")
    public ResponseEntity<FlightDto> bookSeat(@PathVariable Long flightId, @RequestParam String seatNumber) {
        return ResponseEntity.ok(flightService.bookSeat(flightId, seatNumber));
    }

}
