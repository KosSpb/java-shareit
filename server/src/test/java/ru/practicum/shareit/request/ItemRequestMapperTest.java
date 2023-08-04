package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ItemRequestMapperTest {

    @Test
    void mapDtoToItemRequest_whenInvoked_thenReturnItemRequest() {
        User applicant = new User(1L, "Hava", "caly_covelll@tribunal.ir");
        ItemRequestDtoOfRequest itemRequestDtoOfRequest = new ItemRequestDtoOfRequest("need spoon");

        ItemRequest itemRequest = ItemRequestMapper.mapDtoToItemRequest(itemRequestDtoOfRequest, applicant);

        assertThat(itemRequest.getDescription(), equalTo(itemRequestDtoOfRequest.getDescription()));
        assertThat(itemRequest.getApplicant(), equalTo(applicant));
    }

    @Test
    void mapItemRequestToDto_whenInvoked_thenReturnItemRequestDtoOfResponse() {
        User applicant = new User(1L, "Hava", "caly_covelll@tribunal.ir");
        ItemRequest itemRequest = new ItemRequest(1L, "need spoon", LocalDateTime.now(), applicant);

        ItemRequestDtoOfResponse itemRequestDtoOfResponse = ItemRequestMapper.mapItemRequestToDto(itemRequest);

        assertThat(itemRequestDtoOfResponse.getId(), equalTo(itemRequest.getId()));
        assertThat(itemRequestDtoOfResponse.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestDtoOfResponse.getCreated(), equalTo(itemRequest.getCreated()));
    }
}