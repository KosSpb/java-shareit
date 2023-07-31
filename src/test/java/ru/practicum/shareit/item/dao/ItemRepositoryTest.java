package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private PageRequest pageRequest;
    private User owner1;
    private User owner2;
    private User applicant;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private LocalDateTime currentTime = LocalDateTime.now();


    @BeforeEach
    void addData() {
        owner1 = new User(null, "Darus", "kenika_plummeryps@networks.or");
        owner2 = new User(null, "Mayer", "dearon_rourkeynz2@chevy.gp");
        applicant = new User(null, "Hava", "caly_covelll@tribunal.ir");
        itemRequest1 = new ItemRequest(null, "need guitar", currentTime, applicant);
        itemRequest2 = new ItemRequest(null, "need cooler", currentTime, applicant);
        item1 = new Item(null, "spoon", "Cool steel spoon", true, owner1, null);
        item2 = new Item(null, "guitar", "wooden guitar", true, owner2, itemRequest1);
        item3 = new Item(null, "Cooler", "cooler with refrigerator", true, owner1,
                itemRequest2);
        item4 = new Item(null, "cool trampoline", "trampoline 5x5", true, owner2,
                null);

        userRepository.saveAll(List.of(owner1, owner2, applicant));
        itemRequestRepository.saveAll(List.of(itemRequest1, itemRequest2));
        itemRepository.saveAll(List.of(item1, item2, item3, item4));
    }

    @Test
    void findByOwnerWithPagination() {
        pageRequest = PageRequest.of(0, 1);
        Page<Item> foundItems = itemRepository.findByOwner(owner1, pageRequest);

        assertThat(foundItems.getTotalElements(), equalTo(2L));
        assertThat(foundItems.getTotalPages(), equalTo(2));
        assertThat(foundItems.getContent().get(0), equalTo(item1));

        pageRequest = PageRequest.of(1, 1);
        foundItems = itemRepository.findByOwner(owner1, pageRequest);

        assertThat(foundItems.getTotalElements(), equalTo(2L));
        assertThat(foundItems.getTotalPages(), equalTo(2));
        assertThat(foundItems.getContent().get(0), equalTo(item3));
    }

    @Test
    void findByOwner() {
        List<Item> foundItems = itemRepository.findByOwner(owner2);

        assertThat(foundItems.size(), equalTo(2));
        assertThat(foundItems, allOf(hasItem(item4), hasItem(item2)));
    }

    @Test
    void searchItemsByText() {
        pageRequest = PageRequest.of(0, 2);
        Page<Item> foundItems = itemRepository.searchItemsByText("cool", pageRequest);

        assertThat(foundItems.getTotalElements(), equalTo(3L));
        assertThat(foundItems.getTotalPages(), equalTo(2));
        assertThat(foundItems.getContent().get(0), equalTo(item1));
        assertThat(foundItems.getContent().get(1), equalTo(item3));

        pageRequest = PageRequest.of(1, 2);
        foundItems = itemRepository.searchItemsByText("cool", pageRequest);

        assertThat(foundItems.getTotalElements(), equalTo(3L));
        assertThat(foundItems.getTotalPages(), equalTo(2));
        assertThat(foundItems.getContent().get(0), equalTo(item4));
    }

    @Test
    void findByItemRequestIn() {
        List<Item> foundItems = itemRepository.findByItemRequestIn(List.of(itemRequest1, itemRequest2));

        assertThat(foundItems.size(), equalTo(2));
        assertThat(foundItems, allOf(hasItem(item2), hasItem(item3)));
    }
}