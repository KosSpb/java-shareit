package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void createBooking_whenUserAndItemFoundAndBookerIsNotOwnerAndItemIsAvailable_thenReturnBooking() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        Booking booking = BookingMapper.mapDtoToBooking(bookingDto, item, user);
        Booking bookingAfterSave = new Booking(1L, booking.getStart(), booking.getEnd(), booking.getItem(),
                booking.getBooker(), booking.getStatus());
        BookingResponseDto bookingAfterSaveResponseDto = BookingMapper.mapBookingToDto(bookingAfterSave);

        assertThat(booking.getId(), equalTo(null));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(bookingAfterSave);

        BookingResponseDto createBookingResponseDto = bookingService.createBooking(bookingDto, userId);

        assertThat(createBookingResponseDto, equalTo(bookingAfterSaveResponseDto));
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_whenUserNotFound_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        Booking booking = BookingMapper.mapDtoToBooking(bookingDto, item, user);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, userId));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_whenItemNotFound_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        Booking booking = BookingMapper.mapDtoToBooking(bookingDto, item, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, userId));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_whenBookerIsOwner_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Long userId = user.getId();
        Item item = new Item(1L, "lamp", "lamp description", true, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        Booking booking = BookingMapper.mapDtoToBooking(bookingDto, item, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDto, userId));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBooking_whenItemIsNotAvailable_thenNotAvailableExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", false, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        Booking booking = BookingMapper.mapDtoToBooking(bookingDto, item, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class, () -> bookingService.createBooking(bookingDto, userId));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingByOwner_whenBookingFoundAndApproveForTheFirstTimeAndByItemOwner_thenReturnApprovedBooking() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto approveBookingResponseDto =
                bookingService.approveBookingByOwner(anyLong(), true, owner.getId());

        verify(bookingRepository).save(booking);
        assertThat(approveBookingResponseDto.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(approveBookingResponseDto.getId(), equalTo(booking.getId()));
        assertThat(approveBookingResponseDto.getStart(), equalTo(booking.getStart()));
        assertThat(approveBookingResponseDto.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void approveBookingByOwner_whenBookingFoundAndRejectForTheFirstTimeAndByItemOwner_thenReturnRejectedBooking() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto approveBookingResponseDto =
                bookingService.approveBookingByOwner(anyLong(), false, owner.getId());

        verify(bookingRepository).save(booking);
        assertThat(approveBookingResponseDto.getStatus(), equalTo(BookingStatus.REJECTED));
        assertThat(approveBookingResponseDto.getId(), equalTo(booking.getId()));
        assertThat(approveBookingResponseDto.getStart(), equalTo(booking.getStart()));
        assertThat(approveBookingResponseDto.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void approveBookingByOwner_whenBookingNotFound_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.approveBookingByOwner(anyLong(), false, owner.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingByOwner_whenApproveNotByItemOwner_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () ->
                bookingService.approveBookingByOwner(anyLong(), false, booker.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void approveBookingByOwner_whenApproveNotForTheFirstTime_thenAlreadyDoneExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(AlreadyDoneException.class, () ->
                bookingService.approveBookingByOwner(anyLong(), false, owner.getId()));
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void getBookingById_whenBookingFoundAndInvokedByItemOwnerOrBooker_thenReturnBooking() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto approveBookingResponseDto = bookingService.getBookingById(anyLong(), owner.getId());

        assertThat(approveBookingResponseDto, equalTo(bookingResponseDto));
    }

    @Test
    void getBookingById_whenBookingNotFound_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(anyLong(), owner.getId()));
    }

    @Test
    void getBookingById_whenInvokedNotByItemOwnerOrBooker_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(anyLong(), 99L));
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndAllState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerOrderByStartDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.ALL, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByBookerOrderByStartDesc(any(), any());
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndPastState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(3),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerAndEndBeforeOrderByEndDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.PAST, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByBookerAndEndBeforeOrderByEndDesc(any(), any(), any());
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndCurrentState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(3),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.CURRENT, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndFutureState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.FUTURE, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByBookerAndStartAfterOrderByStartDesc(any(), any(), any());
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndWaitingStatus_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.WAITING);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.WAITING, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository)
                .findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING, PageRequest.of(0, 2));
    }

    @Test
    void getAllBookingsOfUser_whenUserFoundAndRejectedStatus_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.REJECTED);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.REJECTED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository
                .findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsOfUser(BookingState.REJECTED, 0, 2, booker.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository)
                .findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED, PageRequest.of(0, 2));
    }

    @Test
    void getAllBookingsOfUser_whenUserNotFound_thenNotFoundExceptionThrown() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingsOfUser(BookingState.REJECTED, 0, 2, booker.getId()));
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndAllState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository.findByItemInOrderByStartDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.ALL, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByItemInOrderByStartDesc(any(), any());
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndPastState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(3),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository.findByItemInAndEndBeforeOrderByEndDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.PAST, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByItemInAndEndBeforeOrderByEndDesc(any(), any(), any());
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndCurrentState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(3),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository.findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.CURRENT, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(), any(), any());
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndFutureState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository.findByItemInAndStartAfterOrderByStartDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.FUTURE, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository).findByItemInAndStartAfterOrderByStartDesc(any(), any(), any());
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndWaitingStatus_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, booker, BookingStatus.WAITING);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.WAITING);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository
                .findByItemInAndStatusOrderByStartDesc(itemsOfOwner, BookingStatus.WAITING, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.WAITING, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository)
                .findByItemInAndStatusOrderByStartDesc(itemsOfOwner, BookingStatus.WAITING, PageRequest.of(0, 2));
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndRejectedStatus_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        List<Item> itemsOfOwner = List.of(item);
        Booking booking = new Booking(1L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item, booker, BookingStatus.REJECTED);
        Booking booking1 = new Booking(2L, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4),
                item, booker, BookingStatus.REJECTED);
        BookingResponseDto bookingResponseDto = BookingMapper.mapBookingToDto(booking);
        BookingResponseDto booking1ResponseDto = BookingMapper.mapBookingToDto(booking1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(itemsOfOwner);
        when(bookingRepository
                .findByItemInAndStatusOrderByStartDesc(itemsOfOwner, BookingStatus.REJECTED, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(booking, booking1)));

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.REJECTED, 0, 2, owner.getId());

        assertThat(allBookings, allOf(hasItem(bookingResponseDto), hasItem(booking1ResponseDto)));
        verify(bookingRepository)
                .findByItemInAndStatusOrderByStartDesc(itemsOfOwner, BookingStatus.REJECTED, PageRequest.of(0, 2));
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundButOwnerHasNoItems_thenReturnEmptyCollection() {
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(owner)).thenReturn(Collections.emptyList());

        Collection<BookingResponseDto> allBookings =
                bookingService.getAllBookingsForItemsOfOwner(BookingState.ALL, 0, 2, owner.getId());

        assertThat(allBookings.size(), equalTo(0));
        verify(bookingRepository, never()).findByItemInOrderByStartDesc(any(), any());
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserNotFound_thenNotFoundExceptionThrown() {
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingsForItemsOfOwner(BookingState.ALL, 0, 2, owner.getId()));
    }
}