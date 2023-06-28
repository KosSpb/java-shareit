package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.PageRequest;
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

    @Query("select b from Booking b " +
            "where b.booker = ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findByBookerAndCurrentTimeOrderByStartDesc(User booker, LocalDateTime currentTime);

    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime currentTime);

    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    List<Booking> findByItemInOrderByStartDesc(List<Item> itemsOfOwner);

    List<Booking> findByItemInAndEndBeforeOrderByEndDesc(List<Item> itemsOfOwner, LocalDateTime currentTime);

    @Query("select b from Booking b " +
            "where b.item in ?1 and b.start < ?2 and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findByItemInAndCurrentTimeOrderByStartDesc(List<Item> itemsOfOwner, LocalDateTime currentTime);

    List<Booking> findByItemInAndStartAfterOrderByStartDesc(List<Item> itemsOfOwner, LocalDateTime currentTime);

    List<Booking> findByItemInAndStatusOrderByStartDesc(List<Item> itemsOfOwner, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item = ?1 and b.start < ?2 and b.end > ?2 and b.status in ?3 " +
            "order by b.start")
    List<BookingShort> findLastBookingWithStartInPartAndEndInFuture(Item item, LocalDateTime currentTime,
                                                                    Collection<BookingStatus> statuses,
                                                                    PageRequest pageRequest);

    BookingShort findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(Item item, LocalDateTime currentTime,
                                                                      Collection<BookingStatus> statuses);

    BookingShort findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(Item item, LocalDateTime currentTime,
                                                                        Collection<BookingStatus> statuses);

    List<Booking>findByItemAndBookerAndEndBefore(Item item, User booker, LocalDateTime currentTime);
}
