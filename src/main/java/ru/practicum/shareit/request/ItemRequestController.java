package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collection;

@RestController
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDtoOfResponse createItemRequest(@RequestBody @Valid ItemRequestDtoOfRequest itemRequestDto,
                                                      @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        ItemRequestDtoOfResponse createdItemRequest = itemRequestService.createItemRequest(itemRequestDto, userId);
        log.info("createItemRequest - item request with id {} was created.", createdItemRequest.getId());
        return createdItemRequest;
    }

    @GetMapping
    public Collection<ItemRequestDtoOfResponse> getAllItemRequestsOfApplicant(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllItemRequestsOfApplicant(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDtoOfResponse> getAllItemRequests(
            @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllItemRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoOfResponse getItemRequestById(@PathVariable(value = "requestId") Long id,
                                                       @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestService.getItemRequestById(id, userId);
    }
}
