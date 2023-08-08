package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestService(ItemRequestRepository itemRequestRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public ItemRequestDtoOfResponse createItemRequest(ItemRequestDtoOfRequest itemRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("createItemRequest - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        return ItemRequestMapper.mapItemRequestToDto(
                itemRequestRepository.save(ItemRequestMapper.mapDtoToItemRequest(itemRequestDto, user)));
    }

    public Collection<ItemRequestDtoOfResponse> getAllItemRequests(int from, int size, Long userId,
                                                                   boolean isItemRequestsOfApplicant) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("getAllItemRequests - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        List<ItemRequest> allItemRequests;
        if (isItemRequestsOfApplicant) {
            allItemRequests = itemRequestRepository.findByApplicantOrderByCreatedDesc(user);
        } else {
            allItemRequests = itemRequestRepository
                    .findByApplicantNotOrderByCreatedDesc(user, PageRequest.of(from > 0 ? from / size : 0, size))
                    .getContent();
        }

        Map<Long, List<ItemResponseDto>> requestedItems =
                itemRepository.findByItemRequestIn(allItemRequests).stream()
                        .map(ItemMapper::mapItemToDto)
                        .collect(groupingBy(ItemResponseDto::getRequestId));

        return allItemRequests.stream()
                .map(ItemRequestMapper::mapItemRequestToDto)
                .peek(itemRequestDtoOfResponse -> {
                    List<ItemResponseDto> items =
                            requestedItems.getOrDefault(itemRequestDtoOfResponse.getId(), Collections.emptyList());
                    itemRequestDtoOfResponse.setItems(items);
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public ItemRequestDtoOfResponse getItemRequestById(Long id, Long userId) {
        ItemRequest itemRequest = itemRequestRepository.findById(id).orElseThrow(() -> {
            log.info("getItemRequestById - item request id not found: {}", id);
            throw new NotFoundException("Запроса с данным id не существует.");
        });
        userRepository.findById(userId).orElseThrow(() -> {
            log.info("getItemRequestById - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        List<ItemResponseDto> requestedItems =
                itemRepository.findByItemRequestIn(List.of(itemRequest)).stream()
                        .map(ItemMapper::mapItemToDto)
                        .collect(Collectors.toList());

        ItemRequestDtoOfResponse itemRequestDto = ItemRequestMapper.mapItemRequestToDto(itemRequest);
        itemRequestDto.setItems(requestedItems);
        return itemRequestDto;
    }
}
