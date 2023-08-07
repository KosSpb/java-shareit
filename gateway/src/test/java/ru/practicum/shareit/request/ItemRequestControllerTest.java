package ru.practicum.shareit.request;

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

@WebMvcTest(controllers = ItemRequestController.class)
@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @MockBean
    private ItemRequestClient itemRequestClient;
    @Autowired
    private MockMvc mvc;

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

        verify(itemRequestClient, never()).getAllItemRequests(anyInt(), anyInt(), anyLong(), anyBoolean());
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

        verify(itemRequestClient, never()).getAllItemRequests(anyInt(), anyInt(), anyLong(), anyBoolean());
    }
}