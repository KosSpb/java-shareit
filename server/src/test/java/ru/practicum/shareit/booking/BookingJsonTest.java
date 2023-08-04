package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.user.dto.UserForBookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingJsonTest {
    @Autowired
    private JacksonTester<BookingResponseDto> jsonOfResponse;

    @Test
    @SneakyThrows
    void shouldSerializeToJson() {
        ItemForBookingDto item = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        BookingResponseDto bookingResponseDto = new BookingResponseDto(1L,
                LocalDateTime.of(2000, 1, 1, 12, 12,12),
                LocalDateTime.of(2000, 1, 2, 12, 12, 12),
                item, booker, BookingStatus.WAITING);

        JsonContent<BookingResponseDto> result = jsonOfResponse.write(bookingResponseDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).hasJsonPath("$.item");
        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).hasJsonPath("$.status");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2000-01-01T12:12:12");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2000-01-02T12:12:12");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("ball");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }
}
