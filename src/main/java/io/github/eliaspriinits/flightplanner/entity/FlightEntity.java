package io.github.eliaspriinits.flightplanner.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "flight")
public class FlightEntity {

    @Id
    private Long id;
    private String destination;
    private LocalDateTime date;
    private float duration;
    private float price;
}
