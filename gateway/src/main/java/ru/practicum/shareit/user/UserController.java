package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        ResponseEntity<Object> createdUser = userClient.createUser(userDto);
        log.info("createUser - request was received for user with email \"{}\" and name \"{}\".", userDto.getEmail(),
                userDto.getName());
        return createdUser;
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return userClient.getUsersList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") Long id) {
        return userClient.getUserById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserDto userDto,
                                             @PathVariable(value = "id", required = false) Long id) {
        ResponseEntity<Object> updatedUser = userClient.updateUser(userDto, id);
        log.info("updateUser - request was received for user with email \"{}\" and id {}.", userDto.getEmail(), id);
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable(value = "id") Long id) {
        userClient.removeUser(id);
    }
}
