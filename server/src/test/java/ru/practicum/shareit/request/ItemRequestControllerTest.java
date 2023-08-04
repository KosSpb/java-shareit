package ru.practicum.shareit.request;

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
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private ItemRequestDtoOfResponse itemRequestDtoOfResponse;

    @Test
    @SneakyThrows
    void createItemRequest_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        ItemRequestDtoOfRequest itemRequestDtoOfRequest =
                new ItemRequestDtoOfRequest("need football ball for tomorrow game");
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "ball", "for football",
                true, lastBooking, nextBooking, comments, 1L);
        List<ItemResponseDto> items = List.of(itemResponseDto);
        itemRequestDtoOfResponse = new ItemRequestDtoOfResponse(1L, "need football ball for tomorrow game",
                LocalDateTime.now(), items);

        when(itemRequestService.createItemRequest(itemRequestDtoOfRequest, 1L))
                .thenReturn(itemRequestDtoOfResponse);

        mvc.perform(post("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDtoOfRequest))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestDtoOfResponse)));

        verify(itemRequestService).createItemRequest(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllItemRequestsOfApplicant_whenInvoked_thenResponseStatusOkWithItemRequestsDtoCollectionInBody() {
        BookingForItemDto lastBooking1 = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking1 = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments1 = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        ItemResponseDto itemResponseDto1 = new ItemResponseDto(1L, "ball", "for football",
                true, lastBooking1, nextBooking1, comments1, 1L);
        List<ItemResponseDto> items1 = List.of(itemResponseDto1);
        itemRequestDtoOfResponse = new ItemRequestDtoOfResponse(1L, "need football ball for tomorrow game",
                LocalDateTime.now(), items1);
        BookingForItemDto lastBooking2 = new BookingForItemDto(3L, 1L);
        BookingForItemDto nextBooking2 = new BookingForItemDto(4L, 2L);
        List<CommentDto> comments2 = List.of(new CommentDto(2L, "good counter, with metal button",
                "Mark", LocalDateTime.now().plusDays(3)));
        ItemResponseDto itemResponseDto2 = new ItemResponseDto(2L, "counter", "for count",
                true, lastBooking2, nextBooking2, comments2, 2L);
        List<ItemResponseDto> items2 = List.of(itemResponseDto2);
        ItemRequestDtoOfResponse itemRequestDtoOfResponse1 =
                new ItemRequestDtoOfResponse(2L, "need counter to count ships",
                        LocalDateTime.now().plusMinutes(1), items2);
        List<ItemRequestDtoOfResponse> itemRequestsOfApplicant =
                List.of(itemRequestDtoOfResponse, itemRequestDtoOfResponse1);

        when(itemRequestService.getAllItemRequests(0, 0, 1L, true))
                .thenReturn(itemRequestsOfApplicant);

        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestsOfApplicant)));

        verify(itemRequestService).getAllItemRequests(anyInt(), anyInt(), anyLong(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getAllItemRequests_whenFromIsNotNegativeAndSizeIsPositive_thenResponseStatusOkWithItemRequestsDtoCollectionInBody() {
        BookingForItemDto lastBooking1 = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking1 = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments1 = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        ItemResponseDto itemResponseDto1 = new ItemResponseDto(1L, "ball", "for football",
                true, lastBooking1, nextBooking1, comments1, 1L);
        List<ItemResponseDto> items1 = List.of(itemResponseDto1);
        itemRequestDtoOfResponse = new ItemRequestDtoOfResponse(1L, "need football ball for tomorrow game",
                LocalDateTime.now(), items1);
        BookingForItemDto lastBooking2 = new BookingForItemDto(3L, 1L);
        BookingForItemDto nextBooking2 = new BookingForItemDto(4L, 2L);
        List<CommentDto> comments2 = List.of(new CommentDto(2L, "good counter, with metal button",
                "Mark", LocalDateTime.now().plusDays(3)));
        ItemResponseDto itemResponseDto2 = new ItemResponseDto(2L, "counter", "for count",
                true, lastBooking2, nextBooking2, comments2, 2L);
        List<ItemResponseDto> items2 = List.of(itemResponseDto2);
        ItemRequestDtoOfResponse itemRequestDtoOfResponse1 =
                new ItemRequestDtoOfResponse(2L, "need counter to count ships",
                        LocalDateTime.now().plusMinutes(1), items2);
        List<ItemRequestDtoOfResponse> itemRequests = List.of(itemRequestDtoOfResponse, itemRequestDtoOfResponse1);

        when(itemRequestService.getAllItemRequests(0, 2, 1L, false)).thenReturn(itemRequests);

        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequests)));

        verify(itemRequestService).getAllItemRequests(0, 2, 1L, false);
    }

    @Test
    @SneakyThrows
    void getAllItemRequests_whenFromIsNegative_thenResponseStatusBadRequest() {
        mvc.perform(get("/requests/all")
                        .param("from", "-1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllItemRequests(anyInt(), anyInt(), anyLong(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getAllItemRequests_whenSizeIsNotPositive_thenResponseStatusBadRequest() {
        mvc.perform(get("/requests/all")
                        .param("from", "1")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllItemRequests(anyInt(), anyInt(), anyLong(), anyBoolean());
    }

    @Test
    @SneakyThrows
    void getItemRequestById_whenInvoked_thenResponseStatusOkWithResponseDtoInBody() {
        Long requestId = 1L;
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "ball", "for football",
                true, lastBooking, nextBooking, comments, 1L);
        List<ItemResponseDto> items = List.of(itemResponseDto);
        itemRequestDtoOfResponse = new ItemRequestDtoOfResponse(1L, "need football ball for tomorrow game",
                LocalDateTime.now(), items);

        when(itemRequestService.getItemRequestById(requestId, 1L)).thenReturn(itemRequestDtoOfResponse);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemRequestDtoOfResponse)));

        verify(itemRequestService).getItemRequestById(anyLong(), anyLong());
    }
}