package ru.practicum.shareit.item.dao.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
@Repository
public class InMemoryItemStorage implements ItemStorage {
    private long generatedId;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<User, List<Item>> ownersOfItems = new HashMap<>();

    @Override
    public Item createItem(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);

        List<Item> itemsOfOwner;
        if (ownersOfItems.containsKey(item.getOwner())) {
            itemsOfOwner = ownersOfItems.get(item.getOwner());
        } else {
            itemsOfOwner = new ArrayList<>();
        }
        itemsOfOwner.add(item);
        ownersOfItems.put(item.getOwner(), itemsOfOwner);

        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Item originalItem = items.get(item.getId());
        if (item.getName() == null) {
            item.setName(originalItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(originalItem.getDescription());
        }
        if (item.getIsAvailable() == null) {
            item.setIsAvailable(originalItem.getIsAvailable());
        }
        items.put(item.getId(), item);

        List<Item> itemsOfOwner = ownersOfItems.get(item.getOwner());
        itemsOfOwner.set(itemsOfOwner.indexOf(originalItem), item);
        ownersOfItems.put(item.getOwner(), itemsOfOwner);

        return item;
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> getAllItemsOfOwner(User user) {
        return ownersOfItems.get(user);
    }

    @Override
    public Collection<Item> searchItemsByText(String text) {
        String lowerCasedText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getIsAvailable)
                .filter((Item item) -> item.getName().toLowerCase().contains(lowerCasedText)
                        || item.getDescription().toLowerCase().contains(lowerCasedText))
                .collect(Collectors.toList());
    }

    private long generateId() {
        return ++generatedId;
    }
}
