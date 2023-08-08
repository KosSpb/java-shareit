package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.enums.BookingState;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @MockBean
    private BookingClient bookingClient;
    @Autowired
    private MockMvc mvc;

    @Test
    @SneakyThrows
    void getAllBookingsForItemsOfOwner_whenSizeIsNotPositive_thenResponseStatusBadRequest() {
        BookingState bookingState = BookingState.ALL;

        mvc.perform(get("/bookings/owner")
                        .param("state", bookingState.toString())
                        .param("from", "1")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsForItemsOfOwner(any(), anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllBookingsForItemsOfOwner_whenFromIsNegative_thenResponseStatusBadRequest() {
        BookingState bookingState = BookingState.ALL;

        mvc.perform(get("/bookings/owner")
                        .param("state", bookingState.toString())
                        .param("from", "-1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsForItemsOfOwner(any(), anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUser_whenSizeIsNotPositive_thenResponseStatusBadRequest() {
        BookingState bookingState = BookingState.FUTURE;

        mvc.perform(get("/bookings")
                        .param("state", bookingState.toString())
                        .param("from", "1")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsOfUser(any(), anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUser_whenFromIsNegative_thenResponseStatusBadRequest() {
        BookingState bookingState = BookingState.FUTURE;

        mvc.perform(get("/bookings")
                        .param("state", bookingState.toString())
                        .param("from", "-1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllBookingsOfUser(any(), anyInt(), anyInt(), anyLong());
    }
}