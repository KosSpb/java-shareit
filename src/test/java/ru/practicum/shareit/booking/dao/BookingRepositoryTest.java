package ru.practicum.shareit.booking.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private PageRequest pageRequest;
    private User booker1;
    private User booker2;
    private User owner;
    private LocalDateTime currentTime = LocalDateTime.now();
    private Set<BookingStatus> allStatuses = Set.of(BookingStatus.REJECTED, BookingStatus.WAITING,
            BookingStatus.APPROVED);
    private Item item1;
    private Item item2;
    private Booking bookingPastTimeApproved;
    private Booking bookingPastTimeWaiting;
    private Booking bookingPastTimeRejected;
    private Booking bookingCurrentTimeApproved;
    private Booking bookingCurrentTimeRejected;
    private Booking bookingCurrentTimeWaiting;
    private Booking bookingFutureTimeWaiting;
    private Booking bookingFutureTimeRejected;
    private Booking bookingFutureTimeApproved;


    @BeforeEach
    void addData() {
        owner = new User(null, "Darus", "kenika_plummeryps@networks.or");
        booker1 = new User(null, "Mayer", "dearon_rourkeynz2@chevy.gp");
        booker2 = new User(null, "Hava", "caly_covelll@tribunal.ir");
        item1 = new Item(null, "spoon", "steel spoon", true, owner, null);
        item2 = new Item(null, "guitar", "wooden guitar", true, owner, null);
        bookingPastTimeApproved = new Booking(null, LocalDateTime.now().minusHours(7),
                LocalDateTime.now().minusHours(6), item1, booker1, BookingStatus.APPROVED);
        bookingPastTimeWaiting = new Booking(null, LocalDateTime.now().minusHours(5),
                LocalDateTime.now().minusHours(4), item1, booker2, BookingStatus.WAITING);
        bookingPastTimeRejected = new Booking(null, LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(2), item2, booker1, BookingStatus.REJECTED);
        bookingCurrentTimeApproved = new Booking(null, LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(1), item2, booker2, BookingStatus.APPROVED);
        bookingCurrentTimeRejected = new Booking(null, LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2), item1, booker1, BookingStatus.REJECTED);
        bookingCurrentTimeWaiting = new Booking(null, LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().plusHours(3), item1, booker2, BookingStatus.WAITING);
        bookingFutureTimeWaiting = new Booking(null, LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4), item1, booker2, BookingStatus.WAITING);
        bookingFutureTimeRejected = new Booking(null, LocalDateTime.now().plusHours(5),
                LocalDateTime.now().plusHours(6), item2, booker1, BookingStatus.REJECTED);
        bookingFutureTimeApproved = new Booking(null, LocalDateTime.now().plusHours(7),
                LocalDateTime.now().plusHours(8), item2, booker2, BookingStatus.APPROVED);

        userRepository.saveAll(List.of(owner, booker1, booker2));
        itemRepository.saveAll(List.of(item1, item2));
        bookingRepository.saveAll(List.of(bookingPastTimeApproved, bookingPastTimeWaiting, bookingPastTimeRejected,
                bookingCurrentTimeApproved, bookingCurrentTimeRejected, bookingCurrentTimeWaiting,
                bookingFutureTimeWaiting, bookingFutureTimeRejected, bookingFutureTimeApproved));
    }

    @Test
    void idGenerationByDbForUserAndItemAndBookingEntities() {
        Supplier<NotFoundException> exception = () -> new NotFoundException("Failure of id generation test.");

        User foundUser1 = userRepository.findById(1L).orElseThrow(exception);
        User foundUser2 = userRepository.findById(2L).orElseThrow(exception);
        User foundUser3 = userRepository.findById(3L).orElseThrow(exception);
        Item foundItem1 = itemRepository.findById(1L).orElseThrow(exception);
        Item foundItem2 = itemRepository.findById(2L).orElseThrow(exception);
        Booking foundBooking1 = bookingRepository.findById(1L).orElseThrow(exception);
        Booking foundBooking2 = bookingRepository.findById(2L).orElseThrow(exception);
        Booking foundBooking3 = bookingRepository.findById(3L).orElseThrow(exception);
        Booking foundBooking4 = bookingRepository.findById(4L).orElseThrow(exception);
        Booking foundBooking5 = bookingRepository.findById(5L).orElseThrow(exception);
        Booking foundBooking6 = bookingRepository.findById(6L).orElseThrow(exception);
        Booking foundBooking7 = bookingRepository.findById(7L).orElseThrow(exception);
        Booking foundBooking8 = bookingRepository.findById(8L).orElseThrow(exception);
        Booking foundBooking9 = bookingRepository.findById(9L).orElseThrow(exception);

        assertThat(foundUser1, equalTo(owner));
        assertThat(foundUser2, equalTo(booker1));
        assertThat(foundUser3, equalTo(booker2));
        assertThat(foundItem1, equalTo(item1));
        assertThat(foundItem2, equalTo(item2));
        assertThat(foundBooking1, equalTo(bookingPastTimeApproved));
        assertThat(foundBooking2, equalTo(bookingPastTimeWaiting));
        assertThat(foundBooking3, equalTo(bookingPastTimeRejected));
        assertThat(foundBooking4, equalTo(bookingCurrentTimeApproved));
        assertThat(foundBooking5, equalTo(bookingCurrentTimeRejected));
        assertThat(foundBooking6, equalTo(bookingCurrentTimeWaiting));
        assertThat(foundBooking7, equalTo(bookingFutureTimeWaiting));
        assertThat(foundBooking8, equalTo(bookingFutureTimeRejected));
        assertThat(foundBooking9, equalTo(bookingFutureTimeApproved));
    }

    @Test
    void findByBookerOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 3);
        Page<Booking> foundBookings = bookingRepository.findByBookerOrderByStartDesc(booker1, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(4L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeRejected));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingCurrentTimeRejected));
        assertThat(foundBookings.getContent().get(2), equalTo(bookingPastTimeRejected));

        pageRequest = PageRequest.of(1, 3);
        foundBookings = bookingRepository.findByBookerOrderByStartDesc(booker1, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(4L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeApproved));
    }

    @Test
    void findByBookerAndEndBeforeOrderByEndDesc() {
        pageRequest = PageRequest.of(0, 3);
        Page<Booking> foundBookings =
                bookingRepository.findByBookerAndEndBeforeOrderByEndDesc(booker1, currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(1));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeRejected));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingPastTimeApproved));
    }

    @Test
    void findByBookerAndStartBeforeAndEndAfterOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 1);
        Page<Booking> foundBookings =
                bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker2, currentTime, currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingCurrentTimeWaiting));

        pageRequest = PageRequest.of(1, 1);
        foundBookings =
                bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker2, currentTime, currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingCurrentTimeApproved));
    }

    @Test
    void findByBookerAndStartAfterOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 2);
        Page<Booking> foundBookings =
                bookingRepository.findByBookerAndStartAfterOrderByStartDesc(booker2, currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(1));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeApproved));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingFutureTimeWaiting));
    }

    @Test
    void findByBookerAndStatusOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 2);
        Page<Booking> foundBookings =
                bookingRepository.findByBookerAndStatusOrderByStartDesc(booker2, BookingStatus.WAITING, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(3L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeWaiting));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingCurrentTimeWaiting));

        pageRequest = PageRequest.of(1, 2);
        foundBookings =
                bookingRepository.findByBookerAndStatusOrderByStartDesc(booker2, BookingStatus.WAITING, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(3L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeWaiting));
    }

    @Test
    void findByItemInOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 3);
        Page<Booking> foundBookings =
                bookingRepository.findByItemInOrderByStartDesc(List.of(item1, item2), pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(9L));
        assertThat(foundBookings.getTotalPages(), equalTo(3));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeApproved));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingFutureTimeRejected));
        assertThat(foundBookings.getContent().get(2), equalTo(bookingFutureTimeWaiting));

        pageRequest = PageRequest.of(1, 3);
        foundBookings = bookingRepository.findByItemInOrderByStartDesc(List.of(item1, item2), pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(9L));
        assertThat(foundBookings.getTotalPages(), equalTo(3));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingCurrentTimeWaiting));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingCurrentTimeRejected));
        assertThat(foundBookings.getContent().get(2), equalTo(bookingCurrentTimeApproved));

        pageRequest = PageRequest.of(2, 3);
        foundBookings = bookingRepository.findByItemInOrderByStartDesc(List.of(item1, item2), pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(9L));
        assertThat(foundBookings.getTotalPages(), equalTo(3));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeRejected));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingPastTimeWaiting));
        assertThat(foundBookings.getContent().get(2), equalTo(bookingPastTimeApproved));
    }

    @Test
    void findByItemInAndEndBeforeOrderByEndDesc() {
        pageRequest = PageRequest.of(0, 2);
        Page<Booking> foundBookings =
                bookingRepository.findByItemInAndEndBeforeOrderByEndDesc(
                        List.of(item1, item2), currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(3L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeRejected));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingPastTimeWaiting));

        pageRequest = PageRequest.of(1, 2);
        foundBookings =
                bookingRepository.findByItemInAndEndBeforeOrderByEndDesc(
                        List.of(item1, item2), currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(3L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingPastTimeApproved));
    }

    @Test
    void findByItemInAndStartBeforeAndEndAfterOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 3);
        Page<Booking> foundBookings =
                bookingRepository.findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(
                        List.of(item1), currentTime, currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(1));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingCurrentTimeWaiting));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingCurrentTimeRejected));
    }

    @Test
    void findByItemInAndStartAfterOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 1);
        Page<Booking> foundBookings =
                bookingRepository.findByItemInAndStartAfterOrderByStartDesc(
                        List.of(item2), currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeApproved));

        pageRequest = PageRequest.of(1, 1);
        foundBookings =
                bookingRepository.findByItemInAndStartAfterOrderByStartDesc(List.of(item2), currentTime, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(2L));
        assertThat(foundBookings.getTotalPages(), equalTo(2));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeRejected));
    }

    @Test
    void findByItemInAndStatusOrderByStartDesc() {
        pageRequest = PageRequest.of(0, 5);
        Page<Booking> foundBookings =
                bookingRepository.findByItemInAndStatusOrderByStartDesc(
                        List.of(item1, item2), BookingStatus.REJECTED, pageRequest);

        assertThat(foundBookings.getTotalElements(), equalTo(3L));
        assertThat(foundBookings.getTotalPages(), equalTo(1));
        assertThat(foundBookings.getContent().get(0), equalTo(bookingFutureTimeRejected));
        assertThat(foundBookings.getContent().get(1), equalTo(bookingCurrentTimeRejected));
        assertThat(foundBookings.getContent().get(2), equalTo(bookingPastTimeRejected));
    }

    @Test
    void findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc() {
        BookingShort bookingShort =
                bookingRepository.findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc(
                        item1, currentTime, currentTime, allStatuses);

        assertThat(bookingShort.getId(), equalTo(bookingCurrentTimeRejected.getId()));
        assertThat(bookingShort.getBooker(), equalTo(bookingCurrentTimeRejected.getBooker()));
    }

    @Test
    void findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc() {
        BookingShort bookingShort =
                bookingRepository.findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(
                        item1, currentTime, allStatuses);

        assertThat(bookingShort.getId(), equalTo(bookingPastTimeWaiting.getId()));
        assertThat(bookingShort.getBooker(), equalTo(bookingPastTimeWaiting.getBooker()));
    }

    @Test
    void findFirstByItemAndStartAfterAndStatusInOrderByStartAsc() {
        BookingShort bookingShort =
                bookingRepository.findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(
                        item2, currentTime, allStatuses);

        assertThat(bookingShort.getId(), equalTo(bookingFutureTimeRejected.getId()));
        assertThat(bookingShort.getBooker(), equalTo(bookingFutureTimeRejected.getBooker()));
    }

    @Test
    void findByItemAndBookerAndEndBefore() {
        List<Booking> foundBookings = bookingRepository.findByItemAndBookerAndEndBefore(item1, booker2, currentTime);

        assertThat(foundBookings.size(), equalTo(1));
        assertThat(foundBookings.get(0), equalTo(bookingPastTimeWaiting));
    }

    @Test
    void findAllLastBookings() {
        List<Booking> foundBookings =
                bookingRepository.findAllLastBookings(List.of(item1, item2), currentTime, allStatuses);

        assertThat(foundBookings.size(), equalTo(2));
        assertThat(foundBookings, allOf(hasItem(bookingPastTimeWaiting), hasItem(bookingPastTimeRejected)));
    }

    @Test
    void findAllCurrentBookings() {
        List<Booking> foundBookings =
                bookingRepository.findAllCurrentBookings(List.of(item1, item2), currentTime, allStatuses);

        assertThat(foundBookings.size(), equalTo(2));
        assertThat(foundBookings, allOf(hasItem(bookingCurrentTimeApproved), hasItem(bookingCurrentTimeWaiting)));
    }

    @Test
    void findAllNextBookings() {
        List<Booking> foundBookings =
                bookingRepository.findAllNextBookings(List.of(item1, item2), currentTime, allStatuses);

        assertThat(foundBookings.size(), equalTo(2));
        assertThat(foundBookings, allOf(hasItem(bookingFutureTimeWaiting), hasItem(bookingFutureTimeRejected)));
    }
}