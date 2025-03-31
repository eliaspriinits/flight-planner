package io.github.eliaspriinits.flightplanner.controller;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.dto.SeatDto;
import io.github.eliaspriinits.flightplanner.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Float minDuration,
            @RequestParam(required = false) Float maxPrice) {

        return ResponseEntity.ok(flightService.filterFlights(destination, date, minDuration, maxPrice));
    }

    @PostMapping("/generate")
    public void generateFlights() {
        flightService.generateFlights();
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<FlightDto>> fetchFlights(@RequestParam(required = false) LocalDateTime date) {
        return ResponseEntity.ok(flightService.getAllFlights());
    }
    @GetMapping("/{flightId}/seats")
    public ResponseEntity<List<String>> fetchSeats(@PathVariable Long flightId) {
        logger.info(flightService.getFlightById(flightId).getSeats().toString());
        return ResponseEntity.ok(flightService.getFlightById(flightId).getAvailableSeats());
    }

    @PutMapping("/{flightId}/booking")
    public ResponseEntity<String> bookSeat(@PathVariable Long flightId, @RequestBody List<String> seatNumbers) {
        for (String seat : seatNumbers) {
            flightService.bookSeat(flightId, seat);
        }
        return ResponseEntity.ok("Seats booked successfully");
    }
}


