package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;
import ru.practicum.shareit.user.User;

public final class ItemRequestMapper {
    public static ItemRequest mapDtoToItemRequest(ItemRequestDtoOfRequest itemRequestDtoOfRequest, User applicant) {
        return ItemRequest.builder()
                .description(itemRequestDtoOfRequest.getDescription())
                .applicant(applicant)
                .build();
    }

    public static ItemRequestDtoOfResponse mapItemRequestToDto(ItemRequest itemRequest) {
        return ItemRequestDtoOfResponse.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
