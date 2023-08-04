package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class BookingMapperTest {

    @Test
    void mapBookingToDto_whenInvoked_thenReturnBookingResponseDto() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        User booker = new User(2L, "Hava", "caly_covelll@tribunal.ir");
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.APPROVED);

        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);

        assertThat(bookingResponseDto.getId(), equalTo(booking.getId()));
        assertThat(bookingResponseDto.getStart(), equalTo(booking.getStart()));
        assertThat(bookingResponseDto.getEnd(), equalTo(booking.getEnd()));
        assertThat(bookingResponseDto.getItem().getId(), equalTo(item.getId()));
        assertThat(bookingResponseDto.getItem().getName(), equalTo(item.getName()));
        assertThat(bookingResponseDto.getBooker().getId(), equalTo(booker.getId()));
        assertThat(bookingResponseDto.getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    void mapDtoToBooking_whenInvoked_thenReturnBooking() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        User booker = new User(2L, "Hava", "caly_covelll@tribunal.ir");
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, null);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item.getId(), LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(3));

        Booking booking = BookingMapper.mapDtoToBooking(bookingRequestDto, item, booker);

        assertThat(booking.getId(), nullValue());
        assertThat(booking.getStart(), equalTo(bookingRequestDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingRequestDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(booker));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }
}