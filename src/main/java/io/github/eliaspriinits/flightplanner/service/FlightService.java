package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.mapper.FlightMapper;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    public void deleteFlight(Long flightId) {
        Optional<FlightEntity> optionalFlight = flightRepository.findById(flightId);
        optionalFlight.ifPresent(flightRepository::delete);
    }

    private List<FlightEntity> filterFlights(List<FlightEntity> flights, Object attribute, String type) {
        List<FlightEntity> flightsFiltered = new ArrayList<>();
        for (FlightEntity flightEntity : flights) {
            if (type.equals("destination") && flightEntity.getDestination().equals(attribute)
            || (type.equals("date") && flightEntity.getDate() == attribute)
            || (type.equals("duration") && flightEntity.getDuration() == attribute)
            || (type.equals("price") && flightEntity.getPrice() == attribute)) {
                flightsFiltered.add(flightEntity);
            }
        }
        return flightsFiltered;
    }


    public List<FlightDto> getFlightsByFilters(Map<String, Object> filters) {
        List<FlightEntity> flights = flightRepository.findAll();
        for (String key : filters.keySet()) {
            flights = filterFlights(flights, filters.get(key), key);
        }
        List<FlightDto> flightDtos = new ArrayList<>();
        for (FlightEntity flightEntity : flights) {
            FlightDto flightDto = flightMapper.toDto(flightEntity);
            flightDtos.add(flightDto);
        }
        return flightDtos;
    }
}
