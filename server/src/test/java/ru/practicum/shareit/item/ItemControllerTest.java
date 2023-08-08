package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private ItemResponseDto itemResponseDto;

    @Test
    @SneakyThrows
    void createItem_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        ItemRequestDto itemRequestDto =
                new ItemRequestDto(null, "ball", "for basketball", true, null);
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        itemResponseDto = new ItemResponseDto(1L, "ball", "for basketball", true,
                lastBooking, nextBooking, comments, null);

        when(itemService.createItem(itemRequestDto, 1L)).thenReturn(itemResponseDto);

        mvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemResponseDto)));

        verify(itemService).createItem(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void updateItem_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        Long itemId = 1L;
        ItemRequestDto itemRequestDto =
                new ItemRequestDto(null, null, "for handball", null, null);
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        itemResponseDto = new ItemResponseDto(1L, "ball", "for handball", true,
                lastBooking, nextBooking, comments, null);

        when(itemService.updateItem(itemRequestDto, itemId, 1L)).thenReturn(itemResponseDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemResponseDto)));

        verify(itemService).updateItem(any(), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void getItemById_whenInvoked_thenResponseStatusOkWithResponseDtoInBody() {
        Long itemId = 1L;
        BookingForItemDto lastBooking = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments = List.of(new CommentDto(1L, "good ball, jovial owner", "Mark",
                LocalDateTime.now().plusDays(2)));
        itemResponseDto = new ItemResponseDto(1L, "ball", "for handball", true,
                lastBooking, nextBooking, comments, null);

        when(itemService.getItemById(itemId, 1L)).thenReturn(itemResponseDto);

        mvc.perform(get("/items/{itemId}", itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemResponseDto)));

        verify(itemService).getItemById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    void getAllItemsOfOwner_whenFromIsNotNegativeAndSizeIsPositive_thenResponseStatusOkWithItemsResponseDtoCollectionInBody() {
        BookingForItemDto lastBooking1 = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking1 = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments1 = List.of(new CommentDto(1L, "good ball, jovial owner",
                "Mark", LocalDateTime.now().plusDays(2)));
        itemResponseDto = new ItemResponseDto(1L, "ball", "for handball", true,
                lastBooking1, nextBooking1, comments1, null);
        BookingForItemDto lastBooking2 = new BookingForItemDto(3L, 1L);
        BookingForItemDto nextBooking2 = new BookingForItemDto(4L, 2L);
        List<CommentDto> comments2 = List.of(new CommentDto(2L, "good game station, looks like new",
                "Carl", LocalDateTime.now().plusDays(3)));
        ItemResponseDto itemResponseDto1 = new ItemResponseDto(2L, "game station", "xbox",
                true, lastBooking2, nextBooking2, comments2, null);
        List<ItemResponseDto> itemsOfOwner = List.of(itemResponseDto, itemResponseDto1);

        when(itemService.getAllItemsOfOwner(0, 2, 1L)).thenReturn(itemsOfOwner);

        mvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemsOfOwner)));

        verify(itemService).getAllItemsOfOwner(0, 2, 1L);
    }

    @Test
    @SneakyThrows
    void searchItemsByText_whenFromIsNotNegativeAndSizeIsPositive_thenResponseStatusOkWithItemsResponseDtoCollectionInBody() {
        String text = "mobile";
        BookingForItemDto lastBooking1 = new BookingForItemDto(1L, 1L);
        BookingForItemDto nextBooking1 = new BookingForItemDto(2L, 2L);
        List<CommentDto> comments1 = List.of(new CommentDto(1L, "good mobile phone with camera",
                "Mark", LocalDateTime.now().plusDays(2)));
        itemResponseDto = new ItemResponseDto(1L, "mobile phone", "for calls",
                true, lastBooking1, nextBooking1, comments1, null);
        BookingForItemDto lastBooking2 = new BookingForItemDto(3L, 1L);
        BookingForItemDto nextBooking2 = new BookingForItemDto(4L, 2L);
        List<CommentDto> comments2 = List.of(new CommentDto(2L, "good game station, looks like new",
                "Carl", LocalDateTime.now().plusDays(3)));
        ItemResponseDto itemResponseDto1 = new ItemResponseDto(2L, "game station", "mobile PSP",
                true, lastBooking2, nextBooking2, comments2, null);
        List<ItemResponseDto> foundItems = List.of(itemResponseDto, itemResponseDto1);

        when(itemService.searchItemsByText(text, 0, 2)).thenReturn(foundItems);

        mvc.perform(get("/items/search")
                        .param("from", "0")
                        .param("size", "2")
                        .param("text", text)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(foundItems)));

        verify(itemService).searchItemsByText(text, 0, 2);
    }

    @Test
    @SneakyThrows
    void createComment_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        Long itemId = 1L;
        CommentDto commentRequestDto = new CommentDto(null, "good ball, jovial owner",
                "Mark", LocalDateTime.now().plusDays(2));
        CommentDto createdComment = new CommentDto(1L, "good ball, jovial owner",
                "Mark", LocalDateTime.now().plusDays(2));

        when(itemService.createComment(commentRequestDto, itemId, 1L)).thenReturn(createdComment);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(createdComment)));

        verify(itemService).createComment(any(), anyLong(), anyLong());
    }
}