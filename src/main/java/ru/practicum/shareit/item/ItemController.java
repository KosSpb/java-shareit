package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto,
                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ItemDto createdItem = itemService.createItem(itemDto, userId);
        log.info("PoI-1. createItem - item \"{}\" with id {} was created.",
                createdItem.getName(), createdItem.getId());
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody @Valid ItemDto itemDto,
                              @PathVariable(value = "itemId") Long id,
                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ItemDto updatedItem = itemService.updateItem(itemDto, id, userId);
        log.info("PaI-1. updateItem - item \"{}\" with id {} was updated.",
                updatedItem.getName(), updatedItem.getId());
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable(value = "itemId") Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping
    public Collection<ItemDto> getAllItemsOfOwner(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsOfOwner(userId);

    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItemsByText(
            @RequestParam(value = "text") String text) {
        return itemService.searchItemsByText(text);
    }
}
