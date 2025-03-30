package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.mapper.FlightMapper;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import io.github.eliaspriinits.flightplanner.service.specifications.FlightSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final FlightApiService flightApiService;

    public void deleteFlight(Long flightId) {
        Optional<FlightEntity> optionalFlight = flightRepository.findById(flightId);
        optionalFlight.ifPresent(flightRepository::delete);
    }

    public void fetchFlights(String origin, String date) {
        flightApiService.fetchAndSaveFlights(origin, date);
    }

    public FlightDto bookSeat(Long flightId, String seatNumber) {
        FlightEntity flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        flight.bookSeat(seatNumber);
        flightRepository.save(flight);

        return FlightMapper.INSTANCE.toDto(flight);
    }


    public List<FlightDto> filterFlights(String destination, LocalDateTime date, Float minDuration, Float maxPrice) {
        List<FlightEntity> flights = flightRepository.findAll();

        return flights.stream()
                .filter(flight -> (destination == null || flight.getDestination().equalsIgnoreCase(destination)))
                .filter(flight -> (date == null || flight.getDate().toLocalDate().equals(date.toLocalDate())))
                .filter(flight -> (minDuration == null || flight.getDuration() >= minDuration))
                .filter(flight -> (maxPrice == null || flight.getPrice() <= maxPrice))
                .filter(flight -> flight.getSeatsAvailable() > 0)
                .map(FlightMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }


    public List<FlightDto> getFlightsByFilters(Map<String, Object> filters) {
        Specification<FlightEntity> specification = Specification.where(null);

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("destination".equals(key) && value instanceof String) {
                specification = specification.and(FlightSpecifications.hasDestination((String) value));
            } else if ("date".equals(key) && value instanceof LocalDateTime) {
                specification = specification.and(FlightSpecifications.hasDate((LocalDateTime) value));
            } else if ("minDuration".equals(key) && value instanceof Float) {
                specification = specification.and(FlightSpecifications.hasMinDuration((Float) value));
            } else if ("maxPrice".equals(key) && value instanceof Float) {
                specification = specification.and(FlightSpecifications.hasMaxPrice((Float) value));
            }
        }

        List<FlightEntity> flights = flightRepository.findAll(specification);

        return flights.stream()
                .map(flightMapper::toDto)
                .collect(Collectors.toList());
    }}
