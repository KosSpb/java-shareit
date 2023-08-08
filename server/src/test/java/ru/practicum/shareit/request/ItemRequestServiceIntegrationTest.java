package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ItemRequestServiceIntegrationTest {
    private final EntityManager entityManager;
    private final ItemRequestService itemRequestService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Test
    void createItemRequest_whenUserFound_thenShouldSaveItemRequest() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white keyboard");
        userRepository.save(user);

        itemRequestService.createItemRequest(itemRequestDto, user.getId());

        TypedQuery<ItemRequest> query =
                entityManager.createQuery("SELECT i FROM ItemRequest i WHERE i.description = :description",
                        ItemRequest.class);
        ItemRequest itemRequestAfterSave =
                query.setParameter("description", itemRequestDto.getDescription()).getSingleResult();

        assertThat(itemRequestAfterSave.getId(), equalTo(1L));
        assertThat(itemRequestAfterSave.getDescription(), equalTo(itemRequestAfterSave.getDescription()));
    }

    @Test
    void getAllItemRequests_whenUserFound_thenReturnItemRequestsCollection() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User owner = new User(2L, "Tashiba", "demetris_patchtgc@lodging.hox");
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequestDtoOfRequest itemRequestDto1 = new ItemRequestDtoOfRequest("need lambo");
        userRepository.saveAll(List.of(applicant, owner, user));
        itemRequestService.createItemRequest(itemRequestDto, applicant.getId());
        itemRequestService.createItemRequest(itemRequestDto1, applicant.getId());

        TypedQuery<ItemRequest> queryForEachItemRequest =
                entityManager.createQuery(
                        "SELECT i FROM ItemRequest i WHERE i.description = :description", ItemRequest.class);
        ItemRequest itemRequestAfterSave =
                queryForEachItemRequest.setParameter(
                        "description", itemRequestDto.getDescription()).getSingleResult();
        ItemRequest itemRequestAfterSave1 =
                queryForEachItemRequest.setParameter(
                        "description", itemRequestDto1.getDescription()).getSingleResult();

        Item item = new Item(1L, "white keyboard", "white keyboard description",
                true, owner, itemRequestAfterSave);
        Item item1 = new Item(2L, "black keyboard", "black keyboard description",
                true, owner, itemRequestAfterSave);
        Item item2 = new Item(3L, "lambo", "countach", true, owner, itemRequestAfterSave1);
        itemRepository.saveAll(List.of(item, item1, item2));

        List<ItemRequestDtoOfResponse> returnedItemRequests =
                new ArrayList<>(itemRequestService.getAllItemRequests(
                        0, 3, user.getId(), false));

        TypedQuery<ItemRequest> queryForAllItemRequest =
                entityManager.createQuery("SELECT i FROM ItemRequest i", ItemRequest.class);
        List<ItemRequest> itemRequestsFromDb = queryForAllItemRequest.getResultList();

        assertThat(2, allOf(equalTo(returnedItemRequests.size()), equalTo(itemRequestsFromDb.size())));
        assertThat(itemRequestAfterSave.getId(),
                allOf(equalTo(returnedItemRequests.get(1).getId()), equalTo(itemRequestsFromDb.get(0).getId())));
        assertThat(itemRequestAfterSave1.getId(),
                allOf(equalTo(returnedItemRequests.get(0).getId()), equalTo(itemRequestsFromDb.get(1).getId())));
        assertThat(itemRequestAfterSave.getDescription(),
                allOf(equalTo(returnedItemRequests.get(1).getDescription()),
                        equalTo(itemRequestsFromDb.get(0).getDescription())));
        assertThat(itemRequestAfterSave1.getDescription(),
                allOf(equalTo(returnedItemRequests.get(0).getDescription()),
                        equalTo(itemRequestsFromDb.get(1).getDescription())));
        assertThat(returnedItemRequests.get(0).getCreated(), notNullValue());
        assertThat(returnedItemRequests.get(1).getCreated(), notNullValue());
        assertThat(itemRequestsFromDb.get(0).getCreated(), notNullValue());
        assertThat(itemRequestsFromDb.get(1).getCreated(), notNullValue());
        assertThat(itemRequestsFromDb.get(0).getApplicant(), equalTo(applicant));
        assertThat(itemRequestsFromDb.get(1).getApplicant(), equalTo(applicant));
        assertThat(returnedItemRequests.get(0).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(returnedItemRequests.get(1).getItems().get(1).getId(), equalTo(item1.getId()));
        assertThat(returnedItemRequests.get(1).getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(returnedItemRequests.get(0).getItems().get(0).getRequestId(),
                equalTo(item2.getItemRequest().getId()));
        assertThat(returnedItemRequests.get(1).getItems().get(1).getRequestId(),
                equalTo(item1.getItemRequest().getId()));
        assertThat(returnedItemRequests.get(1).getItems().get(0).getRequestId(),
                equalTo(item.getItemRequest().getId()));
        assertThat(returnedItemRequests.get(0).getItems().get(0).getDescription(),
                equalTo(item2.getDescription()));
        assertThat(returnedItemRequests.get(1).getItems().get(1).getDescription(),
                equalTo(item1.getDescription()));
        assertThat(returnedItemRequests.get(1).getItems().get(0).getDescription(),
                equalTo(item.getDescription()));
    }
}