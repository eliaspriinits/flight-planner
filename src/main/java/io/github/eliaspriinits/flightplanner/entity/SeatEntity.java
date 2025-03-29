package io.github.eliaspriinits.flightplanner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SeatEntity {
    public enum SeatStatus {BOOKED, AVAILABLE}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber; // example: "12A", "15B"

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private FlightEntity flight;
}