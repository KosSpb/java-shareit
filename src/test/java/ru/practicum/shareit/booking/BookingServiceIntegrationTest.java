package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BookingServiceIntegrationTest {
    private final EntityManager entityManager;
    private final BookingService bookingService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Test
    void createBooking_whenUserAndItemFoundAndBookerIsNotOwnerAndItemIsAvailable_thenShouldSaveBooking() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User user = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, user, null);
        BookingRequestDto bookingDto = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        userRepository.saveAll(List.of(user, booker));
        itemRepository.save(item);

        bookingService.createBooking(bookingDto, userId);

        TypedQuery<Booking> query =
                entityManager.createQuery("SELECT b FROM Booking b WHERE b.item = :item", Booking.class);
        Booking bookingAfterSave = query.setParameter("item", item).getSingleResult();

        assertThat(bookingAfterSave.getId(), equalTo(1L));
        assertThat(bookingAfterSave.getStart(), equalTo(bookingDto.getStart()));
        assertThat(bookingAfterSave.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(bookingAfterSave.getItem(), equalTo(item));
        assertThat(bookingAfterSave.getBooker(), equalTo(booker));
        assertThat(bookingAfterSave.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getAllBookingsForItemsOfOwner_whenUserFoundAndOwnerHasItemsAndAllState_thenReturnBookingsCollection() {
        User booker = new User(2L, "Aarion", "lenny_friedmanhpxd@send.nx");
        Long userId = booker.getId();
        User owner = new User(1L, "Fredie", "jodelle_zajacr9jm@wonder.ed");
        Item item = new Item(1L, "lamp", "lamp description", true, owner, null);
        BookingRequestDto bookingDtoPast = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(5),
                LocalDateTime.now().minusHours(3));
        BookingRequestDto bookingDtoPresent = new BookingRequestDto(item.getId(), LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        BookingRequestDto bookingDtoFuture = new BookingRequestDto(item.getId(), LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5));
        userRepository.saveAll(List.of(owner, booker));
        itemRepository.save(item);
        bookingService.createBooking(bookingDtoFuture, userId);
        bookingService.createBooking(bookingDtoPresent, userId);
        bookingService.createBooking(bookingDtoPast, userId);

        List<BookingResponseDto> returnedBookings =
                new ArrayList<>(bookingService
                        .getAllBookingsForItemsOfOwner(BookingState.ALL, 0, 3, owner.getId()));

        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b", Booking.class);
        List<Booking> bookingsFromDb = query.getResultList();

        assertThat(3, allOf(equalTo(bookingsFromDb.size()), equalTo(returnedBookings.size())));
        assertThat(1L, allOf(equalTo(returnedBookings.get(0).getId()),
                equalTo(bookingsFromDb.get(0).getId())));
        assertThat(2L, allOf(equalTo(returnedBookings.get(1).getId()),
                equalTo(bookingsFromDb.get(1).getId())));
        assertThat(3L, allOf(equalTo(returnedBookings.get(2).getId()),
                equalTo(bookingsFromDb.get(2).getId())));
        assertThat(item.getId(),
                allOf(equalTo(returnedBookings.get(0).getItem().getId()),
                equalTo(bookingsFromDb.get(0).getItem().getId()),
                equalTo(returnedBookings.get(1).getItem().getId()),
                equalTo(bookingsFromDb.get(1).getItem().getId()),
                equalTo(returnedBookings.get(2).getItem().getId()),
                equalTo(bookingsFromDb.get(2).getItem().getId())));
        assertThat(booker.getId(),
                allOf(equalTo(returnedBookings.get(0).getBooker().getId()),
                equalTo(bookingsFromDb.get(0).getBooker().getId()),
                equalTo(returnedBookings.get(1).getBooker().getId()),
                equalTo(bookingsFromDb.get(1).getBooker().getId()),
                equalTo(returnedBookings.get(2).getBooker().getId()),
                equalTo(bookingsFromDb.get(2).getBooker().getId())));
        assertThat(bookingDtoFuture.getStart(),
                allOf(equalTo(returnedBookings.get(0).getStart()), equalTo(bookingsFromDb.get(0).getStart())));
        assertThat(bookingDtoFuture.getEnd(),
                allOf(equalTo(returnedBookings.get(0).getEnd()), equalTo(bookingsFromDb.get(0).getEnd())));
        assertThat(bookingDtoPresent.getStart(),
                allOf(equalTo(returnedBookings.get(1).getStart()), equalTo(bookingsFromDb.get(1).getStart())));
        assertThat(bookingDtoPresent.getEnd(),
                allOf(equalTo(returnedBookings.get(1).getEnd()), equalTo(bookingsFromDb.get(1).getEnd())));
        assertThat(bookingDtoPast.getStart(),
                allOf(equalTo(returnedBookings.get(2).getStart()), equalTo(bookingsFromDb.get(2).getStart())));
        assertThat(bookingDtoPast.getEnd(),
                allOf(equalTo(returnedBookings.get(2).getEnd()), equalTo(bookingsFromDb.get(2).getEnd())));
        assertThat(returnedBookings.get(0).getStatus(), notNullValue());
        assertThat(bookingsFromDb.get(0).getStatus(), notNullValue());
        assertThat(returnedBookings.get(1).getStatus(), notNullValue());
        assertThat(bookingsFromDb.get(1).getStatus(), notNullValue());
        assertThat(returnedBookings.get(2).getStatus(), notNullValue());
        assertThat(bookingsFromDb.get(2).getStatus(), notNullValue());
    }
}