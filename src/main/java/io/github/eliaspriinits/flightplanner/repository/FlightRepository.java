package io.github.eliaspriinits.flightplanner.repository;

import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity, Long> {

    List<FlightEntity> findAll();

    List<FlightEntity> findFlightsByDestination(String destination);
    List<FlightEntity> findFlightsByDate(LocalDateTime date);
    List<FlightEntity> findFlightsByDuration(float duration);
    List<FlightEntity> findFlightsByPrice(float price);
    Optional<FlightEntity> findFlightById(Long id);

}
