package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        log.info("PoU-1. createUser - user with email \"{}\" and id {} was created.",
                createdUser.getEmail(), createdUser.getId());
        return createdUser;
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        return userService.getUsersList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable(value = "id") Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@RequestBody @Valid UserDto userDto,
                              @PathVariable(value = "id", required = false) Long id) {
        UserDto updatedUser = userService.updateUser(userDto, id);
        log.info("PaU-1. updateUser - user with email \"{}\" and id {} was updated.", updatedUser.getEmail(), id);
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable(value = "id") Long id) {
        userService.removeUser(id);
    }
}
