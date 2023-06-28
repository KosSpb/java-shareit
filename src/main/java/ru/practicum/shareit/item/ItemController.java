package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

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
    public ItemResponseDto createItem(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                     @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ItemResponseDto createdItem = itemService.createItem(itemRequestDto, userId);
        log.info("createItem - item \"{}\" with id {} was created.", createdItem.getName(), createdItem.getId());
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                     @PathVariable(value = "itemId") Long id,
                                     @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ItemResponseDto updatedItem = itemService.updateItem(itemRequestDto, id, userId);
        log.info("updateItem - item \"{}\" with id {} was updated.", updatedItem.getName(), updatedItem.getId());
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(@PathVariable(value = "itemId") Long id,
                                       @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(id, userId);
    }

    @GetMapping
    public Collection<ItemResponseDto> getAllItemsOfOwner(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsOfOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> searchItemsByText(
            @RequestParam(value = "text") String text) {
        return itemService.searchItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestBody @Valid CommentDto commentDto,
                                    @PathVariable(value = "itemId") Long id,
                                    @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        CommentDto createdComment = itemService.createComment(commentDto, id, userId);
        log.info("createComment - comment with id {} to item {} was created by user {}.", createdComment.getId(),
                id, userId);
        return createdComment;
    }
}
