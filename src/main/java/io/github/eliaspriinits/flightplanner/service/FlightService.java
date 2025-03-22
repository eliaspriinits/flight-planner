package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.mapper.FlightMapper;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    public void deleteFlight(Long flightId) {
        Optional<FlightEntity> optionalFlight = flightRepository.findById(flightId);
        optionalFlight.ifPresent(flightRepository::delete);
    }

    public List<FlightEntity> filterFlightsByDestination(List<FlightEntity> flights,String destination) {
        List<FlightEntity> flightsFiltered = new ArrayList<>();
        for (FlightEntity flightEntity : flights) {
            if (flightEntity.getDestination().equals(destination)) {

            }
        }
    }


    public List<FlightDto> getFlightsByFilters(HashMap<String, Object> filters) {
        List<FlightEntity> flights = flightRepository.findAll();

        for (String key : filters.keySet()) {
            if
        }
    }
}
