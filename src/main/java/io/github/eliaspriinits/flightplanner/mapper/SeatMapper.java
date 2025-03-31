package io.github.eliaspriinits.flightplanner.mapper;

import io.github.eliaspriinits.flightplanner.dto.SeatDto;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatMapper {
    SeatDto toDto(SeatEntity seatEntity);
}
