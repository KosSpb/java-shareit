package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NoIdInRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Validated(OnCreate.class)
    public UserDto createUser(@Valid UserDto userDto) {
        return UserMapper.mapUserToDto(userStorage.createUser(UserMapper.mapDtoToUser(userDto)));
    }

    public Collection<UserDto> getUsersList() {
        return userStorage.getUsersList().stream()
                .map(UserMapper::mapUserToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public UserDto getUserById(Long id) {
        return UserMapper.mapUserToDto(userStorage.getUserById(id).orElseThrow(() -> {
            log.info("getUserById - user id not found: {}", id);
            throw new NotFoundException("Пользователя с данным id не существует.");
        }));
    }

    public UserDto updateUser(UserDto userDto, Long id) {
        if (userDto.getId() == null && id != null) {
            userDto.setId(id);
        }
        if (userDto.getId() == null && id == null) {
            log.info("updateUser - no id in request: {}", userDto);
            throw new NoIdInRequestException("При обновлении не был передан ID пользователя.");
        }
        if (userDto.getName() == null && userDto.getEmail() == null) {
            log.info("updateUser - no body in request: {}", userDto);
            throw new NoBodyInRequestException("При обновлении не были переданы данные о пользователе.");
        }

        return UserMapper.mapUserToDto(userStorage.updateUser(UserMapper.mapDtoToUser(userDto)).orElseThrow(() -> {
            log.info("updateUser - user id not found: {}", userDto);
            throw new NotFoundException("Пользователя с данным id не существует.");
        }));
    }

    public void removeUser(Long id) {
        User user = userStorage.removeUser(id).orElseThrow(() -> {
            log.info("removeUser - user id '{}' not found", id);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        log.info("removeUser - user with email \"{}\" and id {} was removed.", user.getEmail(), id);
    }
}
