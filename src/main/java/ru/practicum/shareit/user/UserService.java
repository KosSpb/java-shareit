package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            log.info("PoU-1. createUser - no e-mail in request body");
            throw new NoEmailInRequestException("При создании пользователя не был передан email.");
        }
        return UserMapper.mapUserToDto(userStorage.createUser(UserMapper.mapDtoToUser(userDto)).orElseThrow(() -> {
            log.info("PoU-1. createUser - user e-mail is already exists: {}", userDto.getEmail());
            throw new AlreadyExistException("Пользователь с данным email уже существует.");
        }));
    }

    public Collection<UserDto> getUsersList() {
        return userStorage.getUsersList().stream()
                .map(UserMapper::mapUserToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public UserDto getUserById(Long id) {
        return UserMapper.mapUserToDto(userStorage.getUserById(id).orElseThrow(() -> {
            log.info("GU-2. getUserById - user id not found: {}", id);
            throw new NotFoundException("Пользователя с данным id не существует.");
        }));
    }

    public UserDto updateUser(UserDto userDto, Long id) {
        if (userDto.getId() == null && id != null) {
            userDto.setId(id);
        }
        if (userDto.getId() == null && id == null) {
            log.info("PaU-1. updateUser - no id in request: {}", userDto);
            throw new NoIdInRequestException("При обновлении не был передан ID пользователя.");
        }
        if (userDto.getName() == null && userDto.getEmail() == null) {
            log.info("PaU-1. updateUser - no body in request: {}", userDto);
            throw new NoBodyInRequestException("При обновлении не были переданы данные о пользователе.");
        }

        return UserMapper.mapUserToDto(userStorage.updateUser(UserMapper.mapDtoToUser(userDto)).orElseThrow(() -> {
            log.info("PaU-1. updateUser - user id not found: {}", userDto);
            throw new NotFoundException("Пользователя с данным id не существует.");
        }));
    }

    public void removeUser(Long id) {
        User user = userStorage.removeUser(id).orElseThrow(() -> {
            log.info("DU-1. removeUser - user id '{}' not found", id);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        log.info("DU-1. removeUser - user with email \"{}\" and id {} was removed.", user.getEmail(), id);
    }
}
