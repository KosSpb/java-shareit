package ru.practicum.shareit.request.dao;

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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ItemRequestRepositoryTest {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private PageRequest pageRequest;
    private User applicant1;
    private User applicant2;
    private User applicant3;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequest itemRequest3;
    private ItemRequest itemRequest4;
    private ItemRequest itemRequest5;

    @BeforeEach
    void addData() {
        applicant1 = new User(null, "Darus", "kenika_plummeryps@networks.or");
        applicant2 = new User(null, "Mayer", "dearon_rourkeynz2@chevy.gp");
        applicant3 = new User(null, "Hava", "caly_covelll@tribunal.ir");
        itemRequest1 = new ItemRequest(null, "need guitar", null, applicant1);
        itemRequest2 = new ItemRequest(null, "need trampoline", null, applicant2);
        itemRequest3 = new ItemRequest(null, "need spoon", null, applicant2);
        itemRequest4 = new ItemRequest(null, "need cooler", null, applicant3);
        itemRequest5 = new ItemRequest(null, "need playstation", null, applicant3);

        userRepository.saveAll(List.of(applicant1, applicant2, applicant3));
        itemRequestRepository.save(itemRequest1);
        itemRequestRepository.save(itemRequest2);
        itemRequestRepository.save(itemRequest3);
        itemRequestRepository.save(itemRequest4);
        itemRequestRepository.save(itemRequest5);
    }

    @Test
    void idGenerationByDbForItemRequestEntity() {
        Supplier<NotFoundException> exception = () -> new NotFoundException("Failure of id generation test.");

        ItemRequest foundItemRequest1 = itemRequestRepository.findById(1L).orElseThrow(exception);
        ItemRequest foundItemRequest2 = itemRequestRepository.findById(2L).orElseThrow(exception);
        ItemRequest foundItemRequest3 = itemRequestRepository.findById(3L).orElseThrow(exception);
        ItemRequest foundItemRequest4 = itemRequestRepository.findById(4L).orElseThrow(exception);
        ItemRequest foundItemRequest5 = itemRequestRepository.findById(5L).orElseThrow(exception);

        assertThat(foundItemRequest1, equalTo(itemRequest1));
        assertThat(foundItemRequest2, equalTo(itemRequest2));
        assertThat(foundItemRequest3, equalTo(itemRequest3));
        assertThat(foundItemRequest4, equalTo(itemRequest4));
        assertThat(foundItemRequest5, equalTo(itemRequest5));
    }

    @Test
    void findByApplicantOrderByCreatedDesc() {
        List<ItemRequest> foundItemRequests = itemRequestRepository.findByApplicantOrderByCreatedDesc(applicant3);

        assertThat(foundItemRequests.size(), equalTo(2));
        assertThat(foundItemRequests.get(0), equalTo(itemRequest5));
        assertThat(foundItemRequests.get(1), equalTo(itemRequest4));
    }

    @Test
    void findByApplicantNotOrderByCreatedDesc() {
        pageRequest = PageRequest.of(0, 2);
        Page<ItemRequest> foundItemRequests =
                itemRequestRepository.findByApplicantNotOrderByCreatedDesc(applicant1, pageRequest);

        assertThat(foundItemRequests.getTotalElements(), equalTo(4L));
        assertThat(foundItemRequests.getTotalPages(), equalTo(2));
        assertThat(foundItemRequests.getContent().get(0), equalTo(itemRequest5));
        assertThat(foundItemRequests.getContent().get(1), equalTo(itemRequest4));


        pageRequest = PageRequest.of(1, 2);
        foundItemRequests = itemRequestRepository.findByApplicantNotOrderByCreatedDesc(applicant1, pageRequest);

        assertThat(foundItemRequests.getTotalElements(), equalTo(4L));
        assertThat(foundItemRequests.getTotalPages(), equalTo(2));
        assertThat(foundItemRequests.getContent().get(0), equalTo(itemRequest3));
        assertThat(foundItemRequests.getContent().get(1), equalTo(itemRequest2));
    }
}