package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Validated(OnCreate.class)
    public ItemDto createItem(@Valid ItemDto itemDto, Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("createItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        return ItemMapper.mapItemToDto(itemStorage.createItem(ItemMapper.mapDtoToItem(itemDto, user)));
    }

    public ItemDto updateItem(ItemDto itemDto, Long id, Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("updateItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        Item item = itemStorage.getItemById(id).orElseThrow(() -> {
            log.info("updateItem - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        if (!item.getOwner().getId().equals(userId)) {
            log.info("updateItem - user {} trying to update item {} of owner {}", userId, id, item.getOwner().getId());
            throw new AccessDeniedException("Изменять параметры вещи может только её владелец.");
        }
        if (itemDto.getName() == null && itemDto.getDescription() == null && itemDto.getIsAvailable() == null) {
            log.info("updateItem - no body in request: {}", itemDto);
            throw new NoBodyInRequestException("При обновлении не были переданы данные о вещи.");
        }
        if (itemDto.getId() == null || !itemDto.getId().equals(id)) {
            itemDto.setId(id);
        }
        return ItemMapper.mapItemToDto(itemStorage.updateItem(ItemMapper.mapDtoToItem(itemDto, user)));
    }

    public ItemDto getItemById(Long id) {
        return ItemMapper.mapItemToDto(itemStorage.getItemById(id).orElseThrow(() -> {
            log.info("getItemById - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        }));
    }

    public Collection<ItemDto> getAllItemsOfOwner(Long userId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> {
            log.info("getAllItemsOfOwner - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        return itemStorage.getAllItemsOfOwner(user).stream()
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
