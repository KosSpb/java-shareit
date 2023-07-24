package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NoIdInRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Test
    void createUser_whenUserDtoIsValidAndEmailIsUnique_thenSaveUser() {
        UserDto userDto = new UserDto(null, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        when(userRepository.save(any())).thenReturn(user);

        UserDto returnedDto = userService.createUser(userDto);

        assertThat(returnedDto.getId(), equalTo(user.getId()));
        assertThat(returnedDto.getName(), equalTo(user.getName()));
        assertThat(returnedDto.getEmail(), equalTo(user.getEmail()));
        verify(userRepository).save(any());
    }

    @Test
    void createUser_whenUserDtoIsValidAndEmailIsNotUnique_thenAlreadyExistExceptionThrown() {
        UserDto userDto = new UserDto(null, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        when(userRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThrows(AlreadyExistException.class, () -> userService.createUser(userDto));
        verify(userRepository).save(any());
    }

    @Test
    void getUsersList_whenInvoked_thenReturnUsersCollection() {
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        User user1 = new User(2L, "Hope", "claudia_craigfp8@investigations.hsa");

        when(userRepository.findAll()).thenReturn(List.of(user, user1));

        Collection<UserDto> allUsers = userService.getUsersList();
        List<UserDto> allUsersInList = new ArrayList<>(allUsers);

        assertThat(allUsers.size(), equalTo(2));
        assertThat(allUsersInList.get(0).getId(), equalTo(user.getId()));
        assertThat(allUsersInList.get(1).getId(), equalTo(user1.getId()));
        assertThat(allUsersInList.get(0).getName(), equalTo(user.getName()));
        assertThat(allUsersInList.get(1).getName(), equalTo(user1.getName()));
        assertThat(allUsersInList.get(0).getEmail(), equalTo(user.getEmail()));
        assertThat(allUsersInList.get(1).getEmail(), equalTo(user1.getEmail()));
    }

    @Test
    void getUserById_whenUserFound_thenReturnUser() {
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto returnedDto = userService.getUserById(user.getId());

        assertThat(returnedDto.getId(), equalTo(user.getId()));
        assertThat(returnedDto.getName(), equalTo(user.getName()));
        assertThat(returnedDto.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void getUserById_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    void updateUser_whenIdOfUserIsInRequestAndBodyIsNotEmptyAndUserFoundAndOnlyNameFieldToUpdate_thenSaveUpdatedUser() {
        UserDto userDto = new UserDto(null, "Ladaris", null);
        User oldUser = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        User userAfterSave = new User();

        when(userRepository.findById(oldUser.getId())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(userArgumentCaptor.capture())).thenReturn(userAfterSave);

        userService.updateUser(userDto, oldUser.getId());

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(oldUser.getId(), equalTo(savedUser.getId()));
        assertThat(userDto.getName(), equalTo(savedUser.getName()));
        assertThat(oldUser.getEmail(), equalTo(savedUser.getEmail()));

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(anyLong());
        inOrder.verify(userRepository).save(any());
    }

    @Test
    void updateUser_whenIdOfUserIsInRequestAndBodyIsNotEmptyAndUserFoundAndEmailIsUniqueAndOnlyEmailFieldToUpdate_thenSaveUpdatedUser() {
        UserDto userDto = new UserDto(null, null, "kareena_pabstnmt6@explained.hkz");
        User oldUser = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        User userAfterSave = new User();

        when(userRepository.findById(oldUser.getId())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(userArgumentCaptor.capture())).thenReturn(userAfterSave);

        userService.updateUser(userDto, oldUser.getId());

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(oldUser.getId(), equalTo(savedUser.getId()));
        assertThat(oldUser.getName(), equalTo(savedUser.getName()));
        assertThat(userDto.getEmail(), equalTo(savedUser.getEmail()));

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(anyLong());
        inOrder.verify(userRepository).save(any());
    }

    @Test
    void updateUser_whenIdOfUserIsInRequestAndBodyIsNotEmptyAndUserFoundAndEmailIsNotUnique_thenAlreadyExistExceptionThrown() {
        UserDto userDto = new UserDto(null, null, "10@ya.ru");
        User oldUser = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        when(userRepository.findById(oldUser.getId())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(userArgumentCaptor.capture())).thenThrow(DataIntegrityViolationException.class);

        assertThrows(AlreadyExistException.class, () -> userService.updateUser(userDto, oldUser.getId()));

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(oldUser.getId(), equalTo(savedUser.getId()));
        assertThat(oldUser.getName(), equalTo(savedUser.getName()));
        assertThat(userDto.getEmail(), equalTo(savedUser.getEmail()));

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(anyLong());
        inOrder.verify(userRepository).save(any());
    }

    @Test
    void updateUser_whenNoIdOfUserIsInRequest_thenNoIdInRequestExceptionThrown() {
        UserDto userDto = new UserDto(null, null, "kareena_pabstnmt6@explained.hkz");

        assertThrows(NoIdInRequestException.class, () -> userService.updateUser(userDto, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenBodyOfRequestIsEmpty_thenNoBodyInRequestExceptionThrown() {
        UserDto userDto = new UserDto(null, null, null);

        assertThrows(NoBodyInRequestException.class, () -> userService.updateUser(userDto, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenUserNotFound_thenNotFoundExceptionThrown() {
        UserDto userDto = new UserDto(null, "Garrick", null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userDto, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeUser_whenUserFound_thenDeleteUser() {
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.removeUser(user.getId());

        verify(userRepository).deleteById(anyLong());
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findById(anyLong());
        inOrder.verify(userRepository).deleteById(anyLong());
    }

    @Test
    void removeUser_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.removeUser(user.getId()));

        verify(userRepository, never()).deleteById(anyLong());
    }
}