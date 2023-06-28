package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

@Deprecated
public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    Optional<Item> getItemById(Long id);

    Collection<Item> getAllItemsOfOwner(User user);

    Collection<Item> searchItemsByText(String text);
}
