package ru.practicum.shareit.user;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserJsonTest {
    @Autowired
    private JacksonTester<UserDto> jsonOfResponse;

    @Test
    @SneakyThrows
    void shouldSerializeToJson() {
        UserDto userDto = new UserDto(1L, "Pete", "caspar_bagby1@crawford.vs");

        JsonContent<UserDto> result = jsonOfResponse.write(userDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.email");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Pete");
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isEqualTo("caspar_bagby1@crawford.vs");
    }
}
