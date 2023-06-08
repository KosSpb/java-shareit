package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    Optional<User> updateUser(User user);

    Collection<User> getUsersList();

    Optional<User> getUserById(Long id);

    Optional<User> removeUser(Long id);
}
