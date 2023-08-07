package ru.practicum.shareit.item;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @MockBean
    private ItemClient itemClient;
    @Autowired
    private MockMvc mvc;

    @Test
    @SneakyThrows
    void getAllItemsOfOwner_whenSizeIsNotPositive_thenResponseStatusBadRequest() {
        mvc.perform(get("/items")
                        .param("from", "1")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAllItemsOfOwner(anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void searchItemsByText_whenSizeIsNotPositive_thenResponseStatusBadRequest() {
        String text = "mobile";

        mvc.perform(get("/items/search")
                        .param("from", "1")
                        .param("size", "0")
                        .param("text", text)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).searchItemsByText(any(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllItemsOfOwner_whenFromIsNegative_thenResponseStatusBadRequest() {
        mvc.perform(get("/items")
                        .param("from", "-1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAllItemsOfOwner(anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    void searchItemsByText_whenFromIsNegative_thenResponseStatusBadRequest() {
        String text = "mobile";

        mvc.perform(get("/items/search")
                        .param("from", "-1")
                        .param("size", "2")
                        .param("text", text)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).searchItemsByText(any(), anyInt(), anyInt());
    }
}