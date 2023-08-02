package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       BookingRepository bookingRepository,
                       CommentRepository commentRepository,
                       ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Validated(OnCreate.class)
    public ItemResponseDto createItem(@Valid ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("createItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        Item item = ItemMapper.mapDtoToItem(itemRequestDto, user);

        if (itemRequestDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemRequestDto.getRequestId()).orElseThrow(() -> {
                log.info("createItem - item request id not found: {}", userId);
                throw new NotFoundException("Запроса с данным id не существует.");
            });
            item.setItemRequest(itemRequest);
        }

        return ItemMapper.mapItemToDto(itemRepository.save(item));
    }

    public ItemResponseDto updateItem(ItemRequestDto itemRequestDto, Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("updateItem - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.info("updateItem - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        if (!item.getOwner().getId().equals(userId)) {
            log.info("updateItem - user {} trying to update item {} of owner {}", userId, id, item.getOwner().getId());
            throw new AccessDeniedException("Изменять параметры вещи может только её владелец.");
        }
        if (itemRequestDto.getName() == null && itemRequestDto.getDescription() == null &&
                itemRequestDto.getIsAvailable() == null) {
            log.info("updateItem - no body in request: {}", itemRequestDto);
            throw new NoBodyInRequestException("При обновлении не были переданы данные о вещи.");
        }
        if (itemRequestDto.getId() == null || !itemRequestDto.getId().equals(id)) {
            itemRequestDto.setId(id);
        }

        if (itemRequestDto.getName() == null) {
            itemRequestDto.setName(item.getName());
        }
        if (itemRequestDto.getDescription() == null) {
            itemRequestDto.setDescription(item.getDescription());
        }
        if (itemRequestDto.getIsAvailable() == null) {
            itemRequestDto.setIsAvailable(item.getIsAvailable());
        }

        return ItemMapper.mapItemToDto(itemRepository.save(ItemMapper.mapDtoToItem(itemRequestDto, user)));
    }

    public ItemResponseDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.info("getItemById - item id not found: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        ItemResponseDto itemResponseDto = ItemMapper.mapItemToDto(item);
        if (item.getOwner().getId().equals(userId)) {
            addLastAndNextBookingToItemDto(item, itemResponseDto);
        }

        itemResponseDto.setComments(commentRepository.findByItem(item).stream()
                .map(CommentMapper::mapCommentToDto)
                .collect(Collectors.toUnmodifiableList()));

        return itemResponseDto;
    }

    public Collection<ItemResponseDto> getAllItemsOfOwner(int from, int size, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("getAllItemsOfOwner - user id not found: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        Set<BookingStatus> allowableStatuses = Set.of(BookingStatus.WAITING, BookingStatus.APPROVED);
        List<Item> itemsOfOwner = itemRepository.findByOwnerOrderByIdAsc(user, PageRequest.of(from > 0 ? from / size : 0, size))
                .getContent();

        List<Booking> lastBookings = bookingRepository.findAllLastBookings(itemsOfOwner, LocalDateTime.now(),
                allowableStatuses);
        List<Booking> currentBookings = bookingRepository.findAllCurrentBookings(itemsOfOwner, LocalDateTime.now(),
                allowableStatuses);
        List<Booking> nextBookings = bookingRepository.findAllNextBookings(itemsOfOwner, LocalDateTime.now(),
                allowableStatuses);

        List<Comment> commentsOfItems = commentRepository.findByItemIn(itemsOfOwner);

        return itemsOfOwner.stream()
                .map(ItemMapper::mapItemToDto)
                .peek(itemResponseDto -> {
                    Item item = itemsOfOwner.stream()
                            .filter((Item i) -> i.getId().equals(itemResponseDto.getId()))
                            .findAny()
                            .orElseThrow(() -> {
                                log.info("getAllItemsOfOwner - item in stream not found");
                                throw new NotFoundException("Ошибка в работе стрима: " +
                                        "не найден совпадающий id для Item");
                            });

                    addAllBookingsToAllItemDto(itemResponseDto, lastBookings, currentBookings, nextBookings);

                    itemResponseDto.setComments(commentsOfItems.stream()
                            .filter(comment -> comment.getItem().getId().equals(item.getId()))
                            .map(CommentMapper::mapCommentToDto)
                            .collect(Collectors.toUnmodifiableList()));
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<ItemResponseDto> searchItemsByText(String text, int from, int size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.searchItemsByText(text, PageRequest.of(from > 0 ? from / size : 0, size)).stream()
                .map(ItemMapper::mapItemToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public CommentDto createComment(CommentDto commentDto, Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("createComment - user not found, id: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.info("createComment - item not found, id: {}", id);
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        List<Booking> suitableBookings =
                bookingRepository.findByItemAndBookerAndEndBefore(item, user, LocalDateTime.now());

        if (suitableBookings.size() > 0) {
            Comment comment = commentRepository.save(CommentMapper.mapDtoToComment(commentDto, item, user));
            return CommentMapper.mapCommentToDto(comment);
        } else {
            log.info("createComment - user with id {} is commenting not booked item with id {}", userId, id);
            throw new NotAvailableException("Отзыв может оставить только тот пользователь, который брал эту вещь " +
                    "в аренду, и только после окончания срока аренды.");
        }
    }

    private void addLastAndNextBookingToItemDto(Item item, ItemResponseDto itemResponseDto) {
        Set<BookingStatus> allowableStatuses = Set.of(BookingStatus.WAITING, BookingStatus.APPROVED);

        BookingShort lastBooking =
                bookingRepository.findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc(
                        item, LocalDateTime.now(), LocalDateTime.now(), allowableStatuses);

        if (lastBooking == null) {
            lastBooking = bookingRepository.findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(
                    item, LocalDateTime.now(), allowableStatuses);
        }

        BookingShort nextBooking = bookingRepository.findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(
                item, LocalDateTime.now(), allowableStatuses);

        if (lastBooking != null) {
            itemResponseDto.setLastBooking(
                    new BookingForItemDto(lastBooking.getId(), lastBooking.getBooker().getId()));
        }
        if (nextBooking != null) {
            itemResponseDto.setNextBooking(
                    new BookingForItemDto(nextBooking.getId(), nextBooking.getBooker().getId()));
        }
    }

    private void addAllBookingsToAllItemDto(ItemResponseDto itemResponseDto, List<Booking> lastBookings,
                                            List<Booking> currentBookings, List<Booking> nextBookings) {
        Booking lastBooking = null;
        List<Booking> oneLastBooking = currentBookings.stream()
                .filter((Booking b) -> b.getItem().getId().equals(itemResponseDto.getId()))
                .collect(Collectors.toList());
        if (oneLastBooking.size() > 0) {
            lastBooking = oneLastBooking.get(0);
        }

        if (lastBooking == null) {
            oneLastBooking = lastBookings.stream()
                    .filter((Booking b) -> b.getItem().getId().equals(itemResponseDto.getId()))
                    .collect(Collectors.toList());

            if (oneLastBooking.size() > 0) {
                lastBooking = oneLastBooking.get(0);
            }
        }

        Booking nextBooking = null;
        List<Booking> oneNextBooking = nextBookings.stream()
                .filter((Booking b) -> b.getItem().getId().equals(itemResponseDto.getId()))
                .collect(Collectors.toList());
        if (oneNextBooking.size() > 0) {
            nextBooking = oneNextBooking.get(0);
        }

        if (lastBooking != null) {
            itemResponseDto.setLastBooking(
                    new BookingForItemDto(lastBooking.getId(), lastBooking.getBooker().getId()));
        }
        if (nextBooking != null) {
            itemResponseDto.setNextBooking(
                    new BookingForItemDto(nextBooking.getId(), nextBooking.getBooker().getId()));
        }
    }
}
