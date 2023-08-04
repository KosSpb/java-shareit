package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                             @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> createdItem = itemClient.createItem(itemRequestDto, userId);
        log.info("createItem - request was received for item \"{}\".", itemRequestDto.getName());
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                             @PathVariable(value = "itemId") Long id,
                                             @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> updatedItem = itemClient.updateItem(itemRequestDto, id, userId);
        log.info("updateItem - request was received for item \"{}\" with id {}.", itemRequestDto.getName(), id);
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable(value = "itemId") Long id,
                                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemClient.getItemById(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsOfOwner(
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemClient.getAllItemsOfOwner(from, size, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByText(
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(value = "text") String text) {
        return itemClient.searchItemsByText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestBody @Valid CommentDto commentDto,
                                                @PathVariable(value = "itemId") Long id,
                                                @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> createdComment = itemClient.createComment(commentDto, id, userId);
        log.info("createComment - request by user {} was received for comment to item {}.", userId, id);
        return createdComment;
    }
}
