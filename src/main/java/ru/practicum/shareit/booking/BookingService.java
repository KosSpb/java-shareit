package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.AlreadyDoneException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("createBooking - user not found, id: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        Item item = itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(() -> {
            log.info("createBooking - item not found, id: {}", bookingRequestDto.getItemId());
            throw new NotFoundException("Вещи с данным id не существует.");
        });

        if (item.getOwner().equals(user)) {
            log.info("createBooking - owner {} trying to book his item, id: {}", userId, item.getId());
            throw new NotFoundException("Владелец вещи не может её забронировать.");
        }

        if (!item.getIsAvailable()) {
            log.info("createBooking - item not available, id: {}", bookingRequestDto.getItemId());
            throw new NotAvailableException("Вещь недоступна для бронирования.");
        }

        Booking booking = BookingMapper.mapDtoToBooking(bookingRequestDto, item, user);
        return BookingMapper.mapBookingToDto(bookingRepository.save(booking));
    }

    public BookingResponseDto approveBookingByOwner(Long id, Boolean isApproved, Long userId) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.info("approveBookingByOwner - booking not found, id: {}", id);
            throw new NotFoundException("Бронирования с данным id не существует.");
        });

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.info("approveBookingByOwner - user {} trying to approve booking {}", userId, id);
            throw new NotFoundException("Подтверждать или отклонять запрос на бронирование вещи " +
                    "может только её владелец.");
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED) || booking.getStatus().equals(BookingStatus.REJECTED)) {
            log.info("approveBookingByOwner - owner {} trying to approve booking {} again", userId, id);
            throw new AlreadyDoneException("У бронирования уже был изменён статус.");
        }

        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.mapBookingToDto(bookingRepository.save(booking));
    }

    public BookingResponseDto getBookingById(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> {
            log.info("getBookingById - booking not found, id: {}", id);
            throw new NotFoundException("Бронирования с данным id не существует.");
        });

        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.mapBookingToDto(booking);
        } else {
            log.info("getBookingById - unauthorized user {} trying to get booking {}", userId, id);
            throw new NotFoundException("Запрашивать бронирование вещи может только её владелец или " +
                    "автор бронирования.");
        }
    }

    public Collection<BookingResponseDto> getAllBookingsOfUser(BookingState state, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("getAllBookingsOfUser - user not found, id: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        List<Booking> bookingsOfUser = new ArrayList<>();

        switch (state) {
            case ALL:
                bookingsOfUser = bookingRepository.findByBookerOrderByStartDesc(user);
                break;
            case PAST:
                bookingsOfUser = bookingRepository.findByBookerAndEndBeforeOrderByEndDesc(user, LocalDateTime.now());
                break;
            case CURRENT:
                bookingsOfUser = bookingRepository.findByBookerAndCurrentTimeOrderByStartDesc(user,
                        LocalDateTime.now());
                break;
            case FUTURE:
                bookingsOfUser = bookingRepository.findByBookerAndStartAfterOrderByStartDesc(user, LocalDateTime.now());
                break;
            case WAITING:
                bookingsOfUser = bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingsOfUser = bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
        }

        return bookingsOfUser.stream()
                .map(BookingMapper::mapBookingToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public Collection<BookingResponseDto> getAllBookingsForItemsOfOwner(BookingState state, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("getAllBookingsForItemsOfOwner - user not found, id: {}", userId);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        List<Item> itemsOfOwner = itemRepository.findByOwner(user);
        if (itemsOfOwner.size() == 0) {
            return new ArrayList<>();
        }

        List<Booking> bookingsForItemsOfOwner = new ArrayList<>();

        switch (state) {
            case ALL:
                bookingsForItemsOfOwner = bookingRepository.findByItemInOrderByStartDesc(itemsOfOwner);
                break;
            case PAST:
                bookingsForItemsOfOwner = bookingRepository.findByItemInAndEndBeforeOrderByEndDesc(itemsOfOwner,
                        LocalDateTime.now());
                break;
            case CURRENT:
                bookingsForItemsOfOwner = bookingRepository.findByItemInAndCurrentTimeOrderByStartDesc(itemsOfOwner,
                        LocalDateTime.now());
                break;
            case FUTURE:
                bookingsForItemsOfOwner = bookingRepository.findByItemInAndStartAfterOrderByStartDesc(itemsOfOwner,
                        LocalDateTime.now());
                break;
            case WAITING:
                bookingsForItemsOfOwner = bookingRepository.findByItemInAndStatusOrderByStartDesc(itemsOfOwner,
                        BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingsForItemsOfOwner = bookingRepository.findByItemInAndStatusOrderByStartDesc(itemsOfOwner,
                        BookingStatus.REJECTED);
        }

        return bookingsForItemsOfOwner.stream()
                .map(BookingMapper::mapBookingToDto)
                .collect(Collectors.toUnmodifiableList());
    }
}
