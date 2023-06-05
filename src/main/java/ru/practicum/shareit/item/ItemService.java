package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NullAtItemDtoFieldException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("PoI-1. createItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        if (itemDto.getName() == null) {
            log.info("PoI-1. createItem - DTO name is null");
            throw new NullAtItemDtoFieldException("Название вещи не может быть null.");
        }
        if (itemDto.getDescription() == null) {
            log.info("PoI-1. createItem - DTO description is null");
            throw new NullAtItemDtoFieldException("Описание вещи не может быть null.");
        }
        if (itemDto.getIsAvailable() == null) {
            log.info("PoI-1. createItem - DTO availability field is null");
            throw new NullAtItemDtoFieldException("Доступность вещи не может быть null.");
        }

        Item item = itemStorage.createItem(ItemMapper.mapDtoToItem(itemDto, userId));
        List<Long> items = user.getItems();
        items.add(item.getId());
        user.setItems(items);
        return ItemMapper.mapItemToDto(item);
    }

    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("PaI-1. updateItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        Item item = itemStorage.getItemById(id).orElseThrow(() -> {
            log.info("PaI-1. updateItem - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        if (!item.getOwnerId().equals(userId)) {
            log.info("PaI-1. updateItem - user {} trying to update item {} of owner {}", userId, id, item.getOwnerId());
            throw new AccessDeniedException("Изменять параметры вещи может только её владелец.");
        }
        if (itemDto.getName() == null && itemDto.getDescription() == null && itemDto.getIsAvailable() == null) {
            log.info("PaI-1. updateItem - no body in request: {}", itemDto);
            throw new NoBodyInRequestException("При обновлении не были переданы данные о вещи.");
        }
        if (itemDto.getId() == null || !itemDto.getId().equals(id)) {
            itemDto.setId(id);
        }
        return ItemMapper.mapItemToDto(itemStorage.updateItem(ItemMapper.mapDtoToItem(itemDto, userId)));
    }

    public ItemDto getItemById(Long id) {
        return ItemMapper.mapItemToDto(itemStorage.getItemById(id).orElseThrow(() -> {
            log.info("GI-2. getItemById - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        }));
    }

    public Collection<ItemDto> getAllItemsOfOwner(Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("GI-1. getAllItemsOfOwner - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        return itemStorage.getAllItemsOfOwner(user.getItems()).stream()
                .map(ItemMapper::mapItemToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<ItemDto> searchItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemStorage.searchItemsByText(text).stream()
                .map(ItemMapper::mapItemToDto)
                .collect(Collectors.toUnmodifiableList());
    }
}
