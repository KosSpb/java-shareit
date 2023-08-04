package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemForBookingDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ItemMapperTest {

    @Test
    void mapItemToDto_whenInvoked_thenReturnItemResponseDto() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        User applicant = new User(2L, "Hava", "caly_covelll@tribunal.ir");
        ItemRequest itemRequest = new ItemRequest(1L, "need spoon", LocalDateTime.now(), applicant);
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, itemRequest);

        ItemResponseDto itemResponseDto = ItemMapper.mapItemToDto(item);

        assertThat(itemResponseDto.getId(), equalTo(item.getId()));
        assertThat(itemResponseDto.getName(), equalTo(item.getName()));
        assertThat(itemResponseDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemResponseDto.getIsAvailable(), equalTo(item.getIsAvailable()));
        assertThat(itemResponseDto.getRequestId(), equalTo(itemRequest.getId()));
    }

    @Test
    void mapDtoToItem_whenInvoked_thenReturnItem() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        ItemRequestDto itemRequestDto =
                new ItemRequestDto(1L, "spoon", "steel spoon", true, null);

        Item item = ItemMapper.mapDtoToItem(itemRequestDto, owner);

        assertThat(item.getId(), equalTo(itemRequestDto.getId()));
        assertThat(item.getName(), equalTo(itemRequestDto.getName()));
        assertThat(item.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(item.getIsAvailable(), equalTo(itemRequestDto.getIsAvailable()));
        assertThat(item.getOwner(), equalTo(owner));
    }

    @Test
    void mapItemToItemForBookingDto_whenInvoked_thenReturnItemForBookingDto() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, null);

        ItemForBookingDto itemForBookingDto = ItemMapper.mapItemToItemForBookingDto(item);

        assertThat(itemForBookingDto.getId(), equalTo(item.getId()));
        assertThat(itemForBookingDto.getName(), equalTo(item.getName()));
    }
}