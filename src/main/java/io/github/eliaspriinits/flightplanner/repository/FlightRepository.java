package io.github.eliaspriinits.flightplanner.repository;

import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long>, JpaSpecificationExecutor<FlightEntity> {

    List<FlightEntity> findAll();
    List<FlightEntity> findByOriginAndDestinationAndDate(String origin, String destination, LocalDateTime date);
}
