package io.github.eliaspriinits.flightplanner.entity;

import io.github.eliaspriinits.flightplanner.SeatStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "flights")
public class FlightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destination;
    private String origin;
    private LocalDateTime date;
    private float duration;
    private float price;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatEntity> seats;

    public int getSeatsAvailable() {
        return (int) seats.stream().filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE).count();
    }

    public void bookSeat(String seatNumber) {
        SeatEntity seat = seats.stream()
                .filter(s -> s.getSeatNumber().equals(seatNumber) && s.getStatus() == SeatStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seat not available!"));

        seat.setStatus(SeatStatus.BOOKED);
    }

    public void cancelSeat(String seatNumber) {
        SeatEntity seat = seats.stream()
                .filter(s -> s.getSeatNumber().equals(seatNumber) && s.getStatus() == SeatStatus.BOOKED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seat is not booked!"));

        seat.setStatus(SeatStatus.AVAILABLE);
    }
}

