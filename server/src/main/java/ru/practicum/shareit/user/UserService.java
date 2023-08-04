package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NoIdInRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(UserDto userDto) {
        try {
            return UserMapper.mapUserToDto(userRepository.save(UserMapper.mapDtoToUser(userDto)));
        } catch (DataIntegrityViolationException exception) {
            log.info("createUser - user e-mail is already exists: {}", userDto.getEmail());
            throw new AlreadyExistException("Пользователь с данным email уже существует.");
        }
    }

    public Collection<UserDto> getUsersList() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapUserToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public UserDto getUserById(Long id) {
        return UserMapper.mapUserToDto(userRepository.findById(id).orElseThrow(() -> {
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

        User user = userRepository.findById(id).orElseThrow(() -> {
            log.info("updateUser - user id not found: {}", userDto);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });

        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        }
        if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }

        try {
            return UserMapper.mapUserToDto(userRepository.save(UserMapper.mapDtoToUser(userDto)));
        } catch (DataIntegrityViolationException exception) {
            log.info("updateUser - e-mail is already in use: {}", userDto.getEmail());
            throw new AlreadyExistException("Данный email уже занят.");
        }
    }

    public void removeUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.info("removeUser - user id '{}' not found", id);
            throw new NotFoundException("Пользователя с данным id не существует.");
        });
        userRepository.deleteById(id);
        log.info("removeUser - user with email \"{}\" and id {} was removed.", user.getEmail(), id);
    }
}
