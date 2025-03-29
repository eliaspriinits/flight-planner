package io.github.eliaspriinits.flightplanner.repository;

import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Long> {

}
