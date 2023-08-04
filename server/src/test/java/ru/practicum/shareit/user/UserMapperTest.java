package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserForBookingDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UserMapperTest {

    @Test
    void mapUserToDto_whenInvoked_thenReturnUserDto() {
        User user = new User(1L, "Darus", "kenika_plummeryps@networks.or");

        UserDto userDto = UserMapper.mapUserToDto(user);

        assertThat(userDto.getId(), equalTo(user.getId()));
        assertThat(userDto.getName(), equalTo(user.getName()));
        assertThat(userDto.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void mapDtoToUser_whenInvoked_thenReturnUser() {
        UserDto userDto = new UserDto(1L, "Darus", "kenika_plummeryps@networks.or");

        User user = UserMapper.mapDtoToUser(userDto);

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void mapUserToUserForBookingDto_whenInvoked_thenReturnUserForBookingDto() {
        User user = new User(1L, "Darus", "kenika_plummeryps@networks.or");

        UserForBookingDto userForBookingDto = UserMapper.mapUserToUserForBookingDto(user);

        assertThat(userForBookingDto.getId(), equalTo(user.getId()));
    }
}