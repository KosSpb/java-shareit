package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;

public final class UserMapper {
    public static UserDto mapUserToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User mapDtoToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .items(new ArrayList<>())
                .build();
    }
}
