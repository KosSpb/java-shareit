package ru.practicum.shareit.item.dao.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private long generatedId;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item createItem(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        item.setRenterIds(items.get(item.getId()).getRenterIds());

        if (item.getIsAvailable() != null && item.getName() == null && item.getDescription() == null) {
            item.setName(items.get(item.getId()).getName());
            item.setDescription(items.get(item.getId()).getDescription());
        }
        if (item.getIsAvailable() == null && item.getName() != null && item.getDescription() == null) {
            item.setIsAvailable(items.get(item.getId()).getIsAvailable());
            item.setDescription(items.get(item.getId()).getDescription());
        }
        if (item.getIsAvailable() == null && item.getName() == null && item.getDescription() != null) {
            item.setIsAvailable(items.get(item.getId()).getIsAvailable());
            item.setName(items.get(item.getId()).getName());
        }
        if (item.getIsAvailable() != null && item.getName() != null && item.getDescription() == null) {
            item.setDescription(items.get(item.getId()).getDescription());
        }
        if (item.getIsAvailable() == null && item.getName() != null && item.getDescription() != null) {
            item.setIsAvailable(items.get(item.getId()).getIsAvailable());
        }
        if (item.getIsAvailable() != null && item.getName() == null && item.getDescription() != null) {
            item.setName(items.get(item.getId()).getName());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        if (!items.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(items.get(id));
    }

    @Override
    public Collection<Item> getAllItemsOfOwner(List<Long> itemIds) {
        return itemIds.stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> searchItemsByText(String text) {
        return items.values().stream()
                .filter(Item::getIsAvailable)
                .filter((Item item) -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    private long generateId() {
        return ++generatedId;
    }
}
