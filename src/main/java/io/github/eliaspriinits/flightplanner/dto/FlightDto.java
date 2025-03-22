package io.github.eliaspriinits.flightplanner.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightDto {
     private String destination;
     private LocalDateTime date;
     private float duration;
     private float price;
}
