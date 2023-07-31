package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoOfResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
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
class ItemRequestServiceTest {
    @InjectMocks
    private ItemRequestService itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void createItemRequest_whenUserFound_thenSaveItemRequest() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white keyboard");
        ItemRequest itemRequest = new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestDtoOfResponse createItemRequestDtoOfResponse =
                itemRequestService.createItemRequest(itemRequestDto, user.getId());

        assertThat(createItemRequestDtoOfResponse.getId(), equalTo(itemRequest.getId()));
        assertThat(createItemRequestDtoOfResponse.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(createItemRequestDtoOfResponse.getCreated(), equalTo(itemRequest.getCreated()));
        verify(itemRequestRepository).save(any());
    }

    @Test
    void createItemRequest_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white keyboard");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.createItemRequest(itemRequestDto, user.getId()));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getAllItemRequestsOfApplicant_whenUserFound_thenReturnItemRequestsCollection() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User owner = new User(2L, "Tashiba", "demetris_patchtgc@lodging.hox");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequestDtoOfRequest itemRequestDto1 = new ItemRequestDtoOfRequest("need lambo");
        ItemRequest itemRequest =
                new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), applicant);
        ItemRequest itemRequest1 =
                new ItemRequest(2L, itemRequestDto1.getDescription(), LocalDateTime.now(), applicant);
        Item item = new Item(1L, "white keyboard", "white keyboard description",
                true, owner, itemRequest);
        Item item1 = new Item(2L, "black keyboard", "black keyboard description",
                true, owner, itemRequest);
        Item item2 = new Item(3L, "lambo", "countach", true, owner, itemRequest1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(applicant));
        when(itemRequestRepository.findByApplicantOrderByCreatedDesc(applicant))
                .thenReturn(List.of(itemRequest, itemRequest1));
        when(itemRepository.findByItemRequestIn(List.of(itemRequest, itemRequest1)))
                .thenReturn(List.of(item, item1, item2));

        Collection<ItemRequestDtoOfResponse> itemRequestsOfApplicant =
                itemRequestService.getAllItemRequests(0, 0, applicant.getId(), true);
        List<ItemRequestDtoOfResponse> itemRequestsOfApplicantInList = new ArrayList<>(itemRequestsOfApplicant);

        assertThat(itemRequestsOfApplicant.size(), equalTo(2));
        assertThat(itemRequestsOfApplicantInList.get(0).getId(), equalTo(itemRequest.getId()));
        assertThat(itemRequestsOfApplicantInList.get(1).getId(), equalTo(itemRequest1.getId()));
        assertThat(itemRequestsOfApplicantInList.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestsOfApplicantInList.get(1).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(itemRequestsOfApplicantInList.get(0).getCreated(), equalTo(itemRequest.getCreated()));
        assertThat(itemRequestsOfApplicantInList.get(1).getCreated(), equalTo(itemRequest1.getCreated()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(1).getId(), equalTo(item1.getId()));
        assertThat(itemRequestsOfApplicantInList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(0).getRequestId(),
                equalTo(item.getItemRequest().getId()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(1).getRequestId(),
                equalTo(item1.getItemRequest().getId()));
        assertThat(itemRequestsOfApplicantInList.get(1).getItems().get(0).getRequestId(),
                equalTo(item2.getItemRequest().getId()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(0).getDescription(),
                equalTo(item.getDescription()));
        assertThat(itemRequestsOfApplicantInList.get(0).getItems().get(1).getDescription(),
                equalTo(item1.getDescription()));
        assertThat(itemRequestsOfApplicantInList.get(1).getItems().get(0).getDescription(),
                equalTo(item2.getDescription()));
    }

    @Test
    void getAllItemRequestsOfApplicant_whenUserNotFound_thenNotFoundExceptionThrown() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");

        when(userRepository.findById(applicant.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllItemRequests(0, 0, applicant.getId(), true));
    }

    @Test
    void getAllItemRequests_whenUserFound_thenReturnItemRequestsCollection() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User owner = new User(2L, "Tashiba", "demetris_patchtgc@lodging.hox");
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequestDtoOfRequest itemRequestDto1 = new ItemRequestDtoOfRequest("need lambo");
        ItemRequest itemRequest =
                new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), applicant);
        ItemRequest itemRequest1 =
                new ItemRequest(2L, itemRequestDto1.getDescription(), LocalDateTime.now(), applicant);
        Item item = new Item(1L, "white keyboard", "white keyboard description",
                true, owner, itemRequest);
        Item item1 = new Item(2L, "black keyboard", "black keyboard description",
                true, owner, itemRequest);
        Item item2 = new Item(3L, "lambo", "countach", true, owner, itemRequest1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByApplicantNotOrderByCreatedDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(itemRequest, itemRequest1)));
        when(itemRepository.findByItemRequestIn(List.of(itemRequest, itemRequest1)))
                .thenReturn(List.of(item, item1, item2));

        Collection<ItemRequestDtoOfResponse> itemRequests =
                itemRequestService.getAllItemRequests(0, 2, user.getId(), false);
        List<ItemRequestDtoOfResponse> itemRequestsInList = new ArrayList<>(itemRequests);

        assertThat(itemRequests.size(), equalTo(2));
        assertThat(itemRequestsInList.get(0).getId(), equalTo(itemRequest.getId()));
        assertThat(itemRequestsInList.get(1).getId(), equalTo(itemRequest1.getId()));
        assertThat(itemRequestsInList.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestsInList.get(1).getDescription(), equalTo(itemRequest1.getDescription()));
        assertThat(itemRequestsInList.get(0).getCreated(), equalTo(itemRequest.getCreated()));
        assertThat(itemRequestsInList.get(1).getCreated(), equalTo(itemRequest1.getCreated()));
        assertThat(itemRequestsInList.get(0).getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(itemRequestsInList.get(0).getItems().get(1).getId(), equalTo(item1.getId()));
        assertThat(itemRequestsInList.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
        assertThat(itemRequestsInList.get(0).getItems().get(0).getRequestId(), equalTo(item.getItemRequest().getId()));
        assertThat(itemRequestsInList.get(0).getItems().get(1).getRequestId(), equalTo(item1.getItemRequest().getId()));
        assertThat(itemRequestsInList.get(1).getItems().get(0).getRequestId(), equalTo(item2.getItemRequest().getId()));
        assertThat(itemRequestsInList.get(0).getItems().get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(itemRequestsInList.get(0).getItems().get(1).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemRequestsInList.get(1).getItems().get(0).getDescription(), equalTo(item2.getDescription()));
    }

    @Test
    void getAllItemRequests_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllItemRequests(0, 2, user.getId(), false));
    }

    @Test
    void getItemRequestById_whenItemRequestFoundAndUserFound_thenReturnItemRequestContainingItemsCollection() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User owner = new User(2L, "Tashiba", "demetris_patchtgc@lodging.hox");
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequest itemRequest = new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), applicant);
        Item item = new Item(1L, "white keyboard", "white keyboard description",
                true, owner, itemRequest);
        Item item1 = new Item(2L, "black keyboard", "black keyboard description",
                true, owner, itemRequest);

        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findByItemRequestIn(List.of(itemRequest))).thenReturn(List.of(item, item1));

        ItemRequestDtoOfResponse itemRequestDtoOfResponse =
                itemRequestService.getItemRequestById(itemRequest.getId(), user.getId());

        assertThat(itemRequestDtoOfResponse.getId(), equalTo(itemRequest.getId()));
        assertThat(itemRequestDtoOfResponse.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestDtoOfResponse.getCreated(), equalTo(itemRequest.getCreated()));
        assertThat(itemRequestDtoOfResponse.getItems().get(0).getId(), equalTo(item.getId()));
        assertThat(itemRequestDtoOfResponse.getItems().get(1).getId(), equalTo(item1.getId()));
        assertThat(itemRequestDtoOfResponse.getItems().get(0).getRequestId(), equalTo(item.getItemRequest().getId()));
        assertThat(itemRequestDtoOfResponse.getItems().get(1).getRequestId(), equalTo(item1.getItemRequest().getId()));
        assertThat(itemRequestDtoOfResponse.getItems().get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(itemRequestDtoOfResponse.getItems().get(1).getDescription(), equalTo(item1.getDescription()));
    }

    @Test
    void getItemRequestById_whenItemRequestNotFound_thenNotFoundExceptionThrown() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequest itemRequest =
                new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), applicant);

        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestById(itemRequest.getId(), user.getId()));
    }

    @Test
    void getItemRequestById_whenUserNotFound_thenNotFoundExceptionThrown() {
        User applicant = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User user = new User(3L, "Betsie", "janaye_conditze7@devel.oi");
        ItemRequestDtoOfRequest itemRequestDto = new ItemRequestDtoOfRequest("need white or black keyboard");
        ItemRequest itemRequest =
                new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), applicant);

        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestById(itemRequest.getId(), user.getId()));
    }
}