package io.github.eliaspriinits.flightplanner.service.specifications;

import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import jakarta.persistence.TemporalType;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Date;
import java.time.LocalDateTime;

public class FlightSpecifications {

    public static Specification<FlightEntity> hasDestination(String destination) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("destination")), destination.toLowerCase());
    }

    public static Specification<FlightEntity> hasDate(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            Date sqlDate = Date.valueOf(date.toLocalDate()); // convert LocalDateTime to Date (only date part)
            return criteriaBuilder.equal(root.get("date").as(Date.class), sqlDate);
        };
    }
    public static Specification<FlightEntity> hasMinDuration(Float minDuration) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.ge(root.get("duration"), minDuration);
    }

    public static Specification<FlightEntity> hasMaxPrice(Float maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.le(root.get("price"), maxPrice);
    }
}

