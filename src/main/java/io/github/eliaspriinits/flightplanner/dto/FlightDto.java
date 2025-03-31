package io.github.eliaspriinits.flightplanner.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightDto {
     private Long id;
     private String destination;
     private LocalDateTime date;
     private float duration;
     private float price;
     private int seatCapacity;
     private int seatsAvailable;
     private List<String> availableSeats;
     private List<SeatDto> seats;
}
