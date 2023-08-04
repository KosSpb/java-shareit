package ru.practicum.shareit.request;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDtoOfResponse> jsonOfResponse;

    @Test
    @SneakyThrows
    void shouldSerializeToJson() {
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.of(2020, 1, 1, 21, 15, 18)));
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "ball", "for football",
                true, lastBooking, nextBooking, comments, 1L);
        List<ItemResponseDto> items = List.of(itemResponseDto);
        ItemRequestDtoOfResponse itemRequestDtoOfResponse =
                new ItemRequestDtoOfResponse(1L, "need football ball for tomorrow game",
                        LocalDateTime.of(2019, 12, 1, 18, 18, 18), items);

        JsonContent<ItemRequestDtoOfResponse> result = jsonOfResponse.write(itemRequestDtoOfResponse);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("need football ball for tomorrow game");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2019-12-01T18:18:18");
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("name").isEqualTo("ball");
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("description").isEqualTo("for football");
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("available").isEqualTo(true);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("lastBooking").extracting("id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("lastBooking").extracting("bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("nextBooking").extracting("id").isEqualTo(2);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("nextBooking").extracting("bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathArrayValue("$.items").element(0)
                .extracting("requestId").isEqualTo(1);
    }
}
