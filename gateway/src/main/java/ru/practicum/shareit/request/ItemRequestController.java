package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @Autowired
    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestBody @Valid ItemRequestDtoOfRequest itemRequestDto,
                                                    @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> createdItemRequest = itemRequestClient.createItemRequest(itemRequestDto, userId);
        log.info("createItemRequest - item request \"{}\" was received.", itemRequestDto.getDescription());
        return createdItemRequest;
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsOfApplicant(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllItemRequests(0, 0, userId, true);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllItemRequests(from, size, userId, false);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@PathVariable(value = "requestId") Long id,
                                                     @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getItemRequestById(id, userId);
    }
}
