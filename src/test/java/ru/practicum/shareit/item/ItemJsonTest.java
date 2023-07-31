package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemJsonTest {
    @Autowired
    private JacksonTester<ItemResponseDto> jsonOfResponse;

    @Test
    @SneakyThrows
    void shouldSerializeToJson() {
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner",
                "Mark", LocalDateTime.of(2020, 1, 1, 21, 15, 18)));
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "ball", "for basketball",
                true, lastBooking, nextBooking, comments, null);

        JsonContent<ItemResponseDto> result = jsonOfResponse.write(itemResponseDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).hasJsonPath("$.requestId");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("ball");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("for basketball");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathArrayValue("$.comments").element(0)
                .extracting("id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.comments").element(0)
                .extracting("text").isEqualTo("good ball, jovial owner");
        assertThat(result).extractingJsonPathArrayValue("$.comments").element(0)
                .extracting("authorName").isEqualTo("Mark");
        assertThat(result).extractingJsonPathArrayValue("$.comments").element(0)
                .extracting("created").isEqualTo("2020-01-01T21:15:18");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(null);
    }
}
