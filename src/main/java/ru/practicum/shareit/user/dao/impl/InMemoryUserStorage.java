package ru.practicum.shareit.user.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Deprecated
@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private long generatedId;
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailsOfUsers = new HashMap<>();

    @Override
    public User createUser(User user) {
        if (emailsOfUsers.containsKey(user.getEmail())) {
            log.info("createUser - user e-mail is already exists: {}", user.getEmail());
            throw new AlreadyExistException("Пользователь с данным email уже существует.");
        }

        user.setId(generateId());
        users.put(user.getId(), user);
        emailsOfUsers.put(user.getEmail(), user.getId());
        return user;
    }

    @Override
    public Optional<User> updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            return Optional.empty();
        }

        if (user.getName() == null && user.getEmail() != null) {
            updateEmail(user);
            user.setName(users.get(user.getId()).getName());
        }
        if (user.getEmail() == null && user.getName() != null) {
            user.setEmail(users.get(user.getId()).getEmail());
        }
        if (user.getEmail() != null && user.getName() != null) {
            updateEmail(user);
        }
        users.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public Collection<User> getUsersList() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> removeUser(Long id) {
        if (!users.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(users.remove(emailsOfUsers.remove(users.get(id).getEmail())));
    }

    private long generateId() {
        return ++generatedId;
    }

    private void updateEmail(User user) {
        if (emailsOfUsers.containsKey(user.getEmail())
                && !emailsOfUsers.get(user.getEmail()).equals(user.getId())) {
            log.info("updateUser - e-mail is already in use: {}", user.getEmail());
            throw new AlreadyExistException("Данный email уже занят.");
        }
        if (!emailsOfUsers.containsKey(user.getEmail())) {
            emailsOfUsers.put(user.getEmail(), user.getId());
            emailsOfUsers.remove(users.get(user.getId()).getEmail());
        }
    }
}
