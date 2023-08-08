package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @Autowired
    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestBody @Valid BookingRequestDto bookingRequestDto,
                                                @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> createdBooking = bookingClient.createBooking(bookingRequestDto, userId);
        log.info("createBooking - request for booking by user with id {} of item with id {} was received.",
                userId, bookingRequestDto.getItemId());
        return createdBooking;
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBookingByOwner(@PathVariable(value = "bookingId") Long id,
                                                        @RequestParam(value = "approved") Boolean isApproved,
                                                        @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingClient.approveBookingByOwner(id, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable(value = "bookingId") Long id,
                                                 @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingClient.getBookingById(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsOfUser(
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        BookingState bookingState = BookingState.findByState(state);
        return bookingClient.getAllBookingsOfUser(bookingState, from, size, userId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingsForItemsOfOwner(
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        BookingState bookingState = BookingState.findByState(state);
        return bookingClient.getAllBookingsForItemsOfOwner(bookingState, from, size, userId);
    }
}
