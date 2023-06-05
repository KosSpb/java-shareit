package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;

public final class ItemMapper {
    public static ItemDto mapItemToDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.getIsAvailable())
                .rentCounter(item.getRenterIds().size())
                .build();
    }

    public static Item mapDtoToItem(ItemDto itemDto, Long userId) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.getIsAvailable())
                .ownerId(userId)
                .renterIds(new ArrayList<>())
                .build();
    }
}
