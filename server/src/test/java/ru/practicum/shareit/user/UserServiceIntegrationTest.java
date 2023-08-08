package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserServiceIntegrationTest {
    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void getUsersList_whenInvoked_thenReturnUsersCollection() {
        UserDto user1 = new UserDto(null, "Derius", "dewayne_remingtonjqs@diamonds.rf");
        UserDto user2 = new UserDto(null, "Hope", "claudia_craigfp8@investigations.hsa");
        userService.createUser(user1);
        userService.createUser(user2);

        List<UserDto> returnedUsers = new ArrayList<>(userService.getUsersList());

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
        List<User> usersFromDb = query.getResultList();

        assertThat(2, allOf(equalTo(returnedUsers.size()), equalTo(usersFromDb.size())));
        assertThat(1L, allOf(equalTo(returnedUsers.get(0).getId()), equalTo(usersFromDb.get(0).getId())));
        assertThat(2L, allOf(equalTo(returnedUsers.get(1).getId()), equalTo(usersFromDb.get(1).getId())));
        assertThat(user1.getName(),
                allOf(equalTo(returnedUsers.get(0).getName()), equalTo(usersFromDb.get(0).getName())));
        assertThat(user2.getName(),
                allOf(equalTo(returnedUsers.get(1).getName()), equalTo(usersFromDb.get(1).getName())));
        assertThat(user1.getEmail(),
                allOf(equalTo(returnedUsers.get(0).getEmail()), equalTo(usersFromDb.get(0).getEmail())));
        assertThat(user2.getEmail(),
                allOf(equalTo(returnedUsers.get(1).getEmail()), equalTo(usersFromDb.get(1).getEmail())));
    }

    @Test
    void updateUser_whenIdOfUserIsInRequestAndBodyIsNotEmptyAndUserFoundAndOnlyNameFieldToUpdate_thenShouldSaveUpdatedUser() {
        UserDto oldUser = new UserDto(null, "Derius", "dewayne_remingtonjqs@diamonds.rf");

        UserDto oldUserAfterSave = userService.createUser(oldUser);

        assertThat(oldUserAfterSave.getId(), equalTo(1L));
        assertThat(oldUserAfterSave.getName(), equalTo(oldUser.getName()));
        assertThat(oldUserAfterSave.getEmail(), equalTo(oldUser.getEmail()));

        UserDto userDtoToUpdate = new UserDto(null, "Ladaris", null);

        userService.updateUser(userDtoToUpdate, oldUserAfterSave.getId());

        TypedQuery<User> query =
                entityManager.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class);
        User updatedUserAfterSave = query.setParameter("name", userDtoToUpdate.getName()).getSingleResult();

        assertThat(updatedUserAfterSave.getId(), equalTo(oldUserAfterSave.getId()));
        assertThat(updatedUserAfterSave.getName(), equalTo(userDtoToUpdate.getName()));
        assertThat(updatedUserAfterSave.getEmail(), equalTo(oldUserAfterSave.getEmail()));
    }
}