package ru.practicum.shareit.booking.enums;

import java.util.Arrays;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState findByState(String state) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(state))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
    }
}
