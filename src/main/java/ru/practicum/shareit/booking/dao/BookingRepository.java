package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndEndBeforeOrderByEndDesc(User booker, LocalDateTime currentTime);

    List<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker, LocalDateTime currentTimeStart,
                                                                        LocalDateTime currentTimeEnd);

    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime currentTime);

    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByItemInOrderByStartDesc(List<Item> itemsOfOwner);

    List<Booking> findByItemInAndEndBeforeOrderByEndDesc(List<Item> itemsOfOwner, LocalDateTime currentTime);

    List<Booking> findByItemInAndStartBeforeAndEndAfterOrderByStartDesc(List<Item> itemsOfOwner,
                                                                        LocalDateTime currentTimeStart,
                                                                        LocalDateTime currentTimeEnd);

    List<Booking> findByItemInAndStartAfterOrderByStartDesc(List<Item> itemsOfOwner, LocalDateTime currentTime);

    List<Booking> findByItemInAndStatusOrderByStartDesc(List<Item> itemsOfOwner, BookingStatus status);

    BookingShort findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc(
            Item item, LocalDateTime currentTimeStart, LocalDateTime currentTimeEnd,
            Collection<BookingStatus> statuses);

    BookingShort findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(Item item, LocalDateTime currentTime,
                                                                      Collection<BookingStatus> statuses);

    BookingShort findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(Item item, LocalDateTime currentTime,
                                                                        Collection<BookingStatus> statuses);

    List<Booking> findByItemAndBookerAndEndBefore(Item item, User booker, LocalDateTime currentTime);

    @Query("select b from Booking b " +
            "where b.end in " +
            "(select max(b2.end) from Booking b2 " +
            "where b2.item in ?1 and b2.end < ?2 and b2.status in ?3 " +
            "group by b2.item)")
    List<Booking> findAllLastBookings(Collection<Item> items, LocalDateTime currentTime,
                                      Collection<BookingStatus> statuses);

    @Query("select b from Booking b " +
            "where b.start in " +
            "(select max(b2.start) from Booking b2 " +
            "where b2.item in ?1 and b2.start < ?2 and b2.end > ?2 and b2.status in ?3 " +
            "group by b2.item)")
    List<Booking> findAllCurrentBookings(Collection<Item> items, LocalDateTime currentTime,
                                         Collection<BookingStatus> statuses);

    @Query("select b from Booking b " +
            "where b.start in " +
            "(select min(b2.start) from Booking b2 " +
            "where b2.item in ?1 and b2.start > ?2 and b2.status in ?3 " +
            "group by b2.item)")
    List<Booking> findAllNextBookings(Collection<Item> items, LocalDateTime currentTime,
                                      Collection<BookingStatus> statuses);

}
