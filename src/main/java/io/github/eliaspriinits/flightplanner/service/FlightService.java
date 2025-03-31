package io.github.eliaspriinits.flightplanner.service;

import io.github.eliaspriinits.flightplanner.SeatStatus;
import io.github.eliaspriinits.flightplanner.dto.FlightDto;
import io.github.eliaspriinits.flightplanner.entity.FlightEntity;
import io.github.eliaspriinits.flightplanner.entity.SeatEntity;
import io.github.eliaspriinits.flightplanner.mapper.FlightMapper;
import io.github.eliaspriinits.flightplanner.repository.FlightRepository;
import io.github.eliaspriinits.flightplanner.service.specifications.FlightSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    // random cities for testing
    private static final List<String> CITIES = List.of("New York", "London", "Paris", "Berlin", "Tokyo", "Tallinn", "Riga");
    private static final Random RANDOM = new Random();

    public void generateFlights() {
        List<FlightEntity> flights = IntStream.range(0, 10)
                .mapToObj(i -> generateRandomFlight())
                .collect(Collectors.toList());
        flightRepository.saveAll(flights);
    }

    private FlightEntity generateRandomFlight() {
        String origin = CITIES.get(RANDOM.nextInt(CITIES.size()));
        String destination;
        do {
            destination = CITIES.get(RANDOM.nextInt(CITIES.size()));
        } while (destination.equals(origin));

        int seatCount = 150;
        FlightEntity flightEntity = new FlightEntity();
        flightEntity.setDate(LocalDateTime.now().plusDays(RANDOM.nextInt(30)));
        flightEntity.setOrigin(origin);
        flightEntity.setPrice(100 + RANDOM.nextInt(900));
        flightEntity.setDestination(destination);
        flightEntity.setDuration(RANDOM.nextInt(12));

        List<SeatEntity> seats = generateSeats(seatCount, flightEntity);
        flightEntity.setSeats(seats);

        return flightEntity;
    }



    private List<SeatEntity> generateSeats(int seatCount, FlightEntity flight) {
        List<SeatEntity> seats = new ArrayList<>();
        char[] seatLetters = {'A', 'B', 'C', 'D', 'E', 'F'};

        int rows = (int) Math.ceil((double) seatCount / seatLetters.length);
        int seatIndex = 0;

        for (int row = 1; row <= rows && seatIndex < seatCount; row++) {
            for (char seatLetter : seatLetters) {
                if (seatIndex >= seatCount) break;
                SeatEntity seat = new SeatEntity();
                seat.setSeatNumber(row + "" + seatLetter);
                seat.setFlight(flight);
                seat.setStatus(RANDOM.nextInt(2) == 1 ? SeatStatus.BOOKED : SeatStatus.AVAILABLE);
                if (seat.getStatus() == SeatStatus.BOOKED) {
                    seat.setSeatNumber("*");
                }
                seats.add(seat);
                seatIndex++;
            }
        }

        return seats;
    }


    public List<FlightDto> getAllFlights() {
        return flightRepository.findAll().stream().map(flightMapper::toDto).collect(Collectors.toList());
    }

    public FlightDto getFlightById(Long id) {
        Optional<FlightEntity> flight = flightRepository.findById(id);
        if (flight.isPresent()) {
            return flightMapper.toDto(flight.get());
        }
        return null;
    }

    public void deleteFlight(Long flightId) {
        Optional<FlightEntity> optionalFlight = flightRepository.findById(flightId);
        optionalFlight.ifPresent(flightRepository::delete);
    }

    public FlightDto bookSeat(Long flightId, String seatNumber) {
        FlightEntity flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        flight.bookSeat(seatNumber);
        flightRepository.save(flight);

        return FlightMapper.INSTANCE.toDto(flight);
    }


    public List<FlightDto> filterFlights(String destination, LocalDate date, Float minDuration, Float maxPrice) {
        List<FlightEntity> flights = flightRepository.findAll();

        return flights.stream()
                .filter(flight -> (destination == null || flight.getDestination().equalsIgnoreCase(destination)))
                .filter(flight -> (date == null || flight.getDate().toLocalDate().equals(date)))
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
