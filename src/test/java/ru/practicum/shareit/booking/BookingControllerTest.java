package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.user.dto.UserForBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private BookingResponseDto bookingResponseDto;

    @Test
    @SneakyThrows
    void createBooking_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        BookingRequestDto bookingRequestDto =
                new BookingRequestDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        ItemForBookingDto item = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        bookingResponseDto =
                new BookingResponseDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                        item, booker, BookingStatus.WAITING);

        when(bookingService.createBooking(bookingRequestDto, 1L)).thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

        verify(bookingService).createBooking(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void createBooking_whenRequestDtoIsNotValidCauseOfNullStart_thenResponseStatusBadRequest() {
        BookingRequestDto bookingRequestDto =
                new BookingRequestDto(1L, null, LocalDateTime.now().plusHours(2));

        mvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void createBooking_whenRequestDtoIsNotValidCauseOfEndBeforeStart_thenResponseStatusBadRequest() {
        BookingRequestDto bookingRequestDto =
                new BookingRequestDto(1L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(2));

        mvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void approveBookingByOwner_whenInvoked_thenResponseStatusOkWithBookingResponseDtoInBody() {
        Long bookingId = 1L;
        ItemForBookingDto item = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        bookingResponseDto =
                new BookingResponseDto(bookingId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                        item, booker, BookingStatus.APPROVED);

        when(bookingService.approveBookingByOwner(bookingId, true, 1L)).thenReturn(bookingResponseDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

        verify(bookingService).approveBookingByOwner(bookingId, true, 1L);
    }

    @Test
    @SneakyThrows
    void getBookingById_whenInvoked_thenResponseStatusOkWithResponseDtoInBody() {
        Long bookingId = 1L;
        ItemForBookingDto item = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        bookingResponseDto =
                new BookingResponseDto(bookingId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                        item, booker, BookingStatus.APPROVED);

        when(bookingService.getBookingById(bookingId, 1L)).thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

        verify(bookingService).getBookingById(bookingId, 1L);
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUser_whenStateIsValidAndFromIsNotNegativeAndSizeIsPositive_thenResponseStatusOkWithBookingsResponseDtoCollectionInBody() {
        BookingState bookingState = BookingState.FUTURE;
        ItemForBookingDto item1 = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        bookingResponseDto = new BookingResponseDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item1, booker, BookingStatus.APPROVED);
        ItemForBookingDto item2 = new ItemForBookingDto(2L, "basket");
        BookingResponseDto bookingResponseDto2 = new BookingResponseDto(2L, LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4), item2, booker, BookingStatus.APPROVED);
        List<BookingResponseDto> bookingsOfUser = List.of(bookingResponseDto, bookingResponseDto2);

        when(bookingService.getAllBookingsOfUser(bookingState, 0, 2, 1L)).thenReturn(bookingsOfUser);

        mvc.perform(get("/bookings")
                        .param("state", bookingState.toString())
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingsOfUser)));

        verify(bookingService).getAllBookingsOfUser(bookingState, 0, 2, 1L);
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUser_whenStateIsNotValid_thenResponseStatusBadRequest() {
        mvc.perform(get("/bookings")
                        .param("state", "WRONG state")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsOfUser(any(), anyInt(), anyInt(), anyLong());
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

        verify(bookingService, never()).getAllBookingsOfUser(any(), anyInt(), anyInt(), anyLong());
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

        verify(bookingService, never()).getAllBookingsOfUser(any(), anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllBookingsForItemsOfOwner_whenStateIsValidAndFromIsNotNegativeAndSizeIsPositive_thenResponseStatusOkWithBookingsResponseDtoCollectionInBody() {
        BookingState bookingState = BookingState.ALL;
        ItemForBookingDto item1 = new ItemForBookingDto(1L, "ball");
        UserForBookingDto booker = new UserForBookingDto(1L);
        bookingResponseDto = new BookingResponseDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), item1, booker, BookingStatus.APPROVED);
        ItemForBookingDto item2 = new ItemForBookingDto(2L, "basket");
        BookingResponseDto bookingResponseDto2 = new BookingResponseDto(2L, LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4), item2, booker, BookingStatus.WAITING);
        List<BookingResponseDto> bookingsOfUser = List.of(bookingResponseDto, bookingResponseDto2);

        when(bookingService.getAllBookingsForItemsOfOwner(bookingState, 0, 2, 1L))
                .thenReturn(bookingsOfUser);

        mvc.perform(get("/bookings/owner")
                        .param("state", bookingState.toString())
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingsOfUser)));

        verify(bookingService).getAllBookingsForItemsOfOwner(bookingState, 0, 2, 1L);
    }

    @Test
    @SneakyThrows
    void getAllBookingsForItemsOfOwner_whenStateIsNotValid_thenResponseStatusBadRequest() {
        mvc.perform(get("/bookings/owner")
                        .param("state", "WRONG state")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getAllBookingsForItemsOfOwner(any(), anyInt(), anyInt(), anyLong());
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

        verify(bookingService, never()).getAllBookingsForItemsOfOwner(any(), anyInt(), anyInt(), anyLong());
    }

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

        verify(bookingService, never()).getAllBookingsForItemsOfOwner(any(), anyInt(), anyInt(), anyLong());
    }
}