package io.github.eliaspriinits.flightplanner.dto;

import io.github.eliaspriinits.flightplanner.SeatStatus;
import lombok.Data;

@Data
public class SeatDto {
    private String seatNumber;
    private SeatStatus status;
}
