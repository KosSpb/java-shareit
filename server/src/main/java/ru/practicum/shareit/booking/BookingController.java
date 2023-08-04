package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestBody @Valid BookingRequestDto bookingRequestDto,
                                            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        BookingResponseDto createdBooking = bookingService.createBooking(bookingRequestDto, userId);
        log.info("createBooking - booking by user with id {} of item: \"{}\", with id {} was created.",
                userId, createdBooking.getItem(), createdBooking.getId());
        return createdBooking;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBookingByOwner(@PathVariable(value = "bookingId") Long id,
                                                    @RequestParam(value = "approved") Boolean isApproved,
                                                    @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingService.approveBookingByOwner(id, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable(value = "bookingId") Long id,
                                             @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(id, userId);
    }

    @GetMapping
    public Collection<BookingResponseDto> getAllBookingsOfUser(
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingService.getAllBookingsOfUser(BookingState.valueOf(state), from, size, userId);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getAllBookingsForItemsOfOwner(
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return bookingService.getAllBookingsForItemsOfOwner(BookingState.valueOf(state), from, size, userId);
    }
}
