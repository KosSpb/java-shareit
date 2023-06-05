package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    Optional<Item> getItemById(Long id);

    Collection<Item> getAllItemsOfOwner(List<Long> itemIds);

    Collection<Item> searchItemsByText(String text);
}
