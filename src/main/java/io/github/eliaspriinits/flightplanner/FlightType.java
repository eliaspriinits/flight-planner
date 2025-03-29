package io.github.eliaspriinits.flightplanner;


import lombok.Getter;

@Getter
public enum FlightType {
    SMALL(50, "Small Regional Jet"),
    MEDIUM(150, "Medium Commercial Jet"),
    LARGE(300, "Large Wide-Body Jet"),
    SUPER_JUMBO(600, "Super Jumbo Jet");

    private final int maxSeats;
    private final String description;

    FlightType(int maxSeats, String description) {
        this.maxSeats = maxSeats;
        this.description = description;
    }


    public static FlightType fromSeats(int seatCount) {
        if (seatCount <= 50) {
            return SMALL;
        } else if (seatCount <= 150) {
            return MEDIUM;
        } else if (seatCount <= 300) {
            return LARGE;
        } else {
            return SUPER_JUMBO;
        }
    }
}

