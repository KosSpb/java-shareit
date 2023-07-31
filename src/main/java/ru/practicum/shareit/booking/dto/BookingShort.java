package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.user.User;

public interface BookingShort {

    Long getId();

    User getBooker();

    void setId(Long id);

    void setBooker(User user);

}
