package io.github.eliaspriinits.flightplanner.mapper;

import io.github.eliaspriinits.flightplanner.SeatStatus;
import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightMapper {
    FlightMapper INSTANCE = Mappers.getMapper(FlightMapper.class);

    @Mapping(target = "seatsAvailable", expression = "java(flight.getSeatsAvailable())")
    @Mapping(target = "availableSeats", expression = "java(getAvailableSeatNumbers(flight))")
    FlightDto toDto(FlightEntity flight);

    FlightEntity toEntity(FlightDto flightDto);

    default List<String> getAvailableSeatNumbers(FlightEntity flight) {
        return flight.getSeats().stream()
                .map(SeatEntity::getSeatNumber)
                .collect(Collectors.toList());
    }
}