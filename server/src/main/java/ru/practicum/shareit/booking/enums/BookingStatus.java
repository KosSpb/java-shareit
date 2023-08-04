package ru.practicum.shareit.booking.enums;

import java.util.Arrays;

public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;

    public static BookingStatus findByStatus(String status) {
        return Arrays.stream(values())
                .filter(bookingStatus -> bookingStatus.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + status));
    }
}
