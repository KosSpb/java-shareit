package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    private UserDto userDto;

    @Test
    @SneakyThrows
    void createUser_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        UserDto userRequestDto = new UserDto(null, "Pete", "akela_stottsj@shoe.bx");
        userDto = new UserDto(1L, "Pete", "akela_stottsj@shoe.bx");

        when(userService.createUser(userRequestDto)).thenReturn(userDto);

        mvc.perform(post("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService).createUser(any());
    }

    @Test
    @SneakyThrows
    void getAllUsers_whenInvoked_thenResponseStatusOkWithUserDtoCollectionInBody() {
        userDto = new UserDto(1L, "Pete", "akela_stottsj@shoe.bx");
        UserDto userDto1 = new UserDto(2L, "Mark", "dyana_jaynencdh@asin.auh");
        List<UserDto> users = List.of(userDto, userDto1);

        when(userService.getUsersList()).thenReturn(users);

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));

        verify(userService).getUsersList();
    }

    @Test
    @SneakyThrows
    void getUserById_whenInvoked_thenResponseStatusOkWithResponseDtoInBody() {
        Long userId = 1L;
        userDto = new UserDto(1L, "Pete", "akela_stottsj@shoe.bx");

        when(userService.getUserById(userId)).thenReturn(userDto);

        mvc.perform(get("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService).getUserById(anyLong());
    }

    @Test
    @SneakyThrows
    void updateUser_whenRequestDtoIsValid_thenResponseStatusOkWithResponseDtoInBody() {
        Long userId = 1L;
        UserDto userRequestDto = new UserDto(null, null, "johnda_salmons2a@heavy.co");
        userDto = new UserDto(1L, "Pete", "johnda_salmons2a@heavy.co");

        when(userService.updateUser(userRequestDto, userId)).thenReturn(userDto);

        mvc.perform(patch("/users/{id}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService).updateUser(any(), anyLong());
    }

    @Test
    @SneakyThrows
    void removeUser_whenInvoked_thenResponseStatusOk() {
        Long userId = 1L;

        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).removeUser(anyLong());
    }
}