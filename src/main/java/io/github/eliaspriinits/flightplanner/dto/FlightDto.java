package io.github.eliaspriinits.flightplanner.dto;

import io.github.eliaspriinits.flightplanner.FlightType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightDto {
     private String destination;
     private LocalDateTime date;
     private float duration;
     private float price;
     private int seatCapacity;
     private int seatsAvailable;
     private List<String> availableSeats;

     public int getSeatsAvailable() {
          return availableSeats != null ? availableSeats.size() : 0;
     }
}
