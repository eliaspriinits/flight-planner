package io.github.eliaspriinits.flightplanner.entity;

import io.github.eliaspriinits.flightplanner.SeatStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "seats")
public class SeatEntity {
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