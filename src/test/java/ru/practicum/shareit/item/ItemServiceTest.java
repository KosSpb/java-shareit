package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NoBodyInRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @InjectMocks
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @Test
    void createItem_whenUserFoundAndItemRequestDtoIsValidAndNoRequestIdInBody_thenSaveItemWithoutRequestId() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "keyboard",
                "keyboard description", true, null);
        Item item = ItemMapper.mapDtoToItem(itemRequestDto, user);
        Item itemAfterSave = new Item(1L, item.getName(), item.getDescription(),
                item.getIsAvailable(), item.getOwner(), item.getItemRequest());
        ItemResponseDto itemAfterSaveDto = ItemMapper.mapItemToDto(itemAfterSave);

        assertThat(itemRequestDto.getRequestId(), equalTo(null));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.save(item)).thenReturn(itemAfterSave);

        ItemResponseDto createItemResponseDto = itemService.createItem(itemRequestDto, user.getId());

        assertThat(createItemResponseDto, equalTo(itemAfterSaveDto));
        verify(itemRepository).save(item);
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void createItem_whenUserFoundAndItemRequestDtoIsValidAndRequestIdInBody_thenSaveItemWithRequestId() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "keyboard",
                "keyboard description", true, 1L);
        Item item = ItemMapper.mapDtoToItem(itemRequestDto, user);
        ItemRequest requestOfItem = new ItemRequest(1L, "need stick", LocalDateTime.now(), new User());
        item.setItemRequest(requestOfItem);
        Item itemAfterSave = new Item(1L, item.getName(), item.getDescription(), item.getIsAvailable(),
                item.getOwner(), item.getItemRequest());
        ItemResponseDto itemAfterSaveDto = ItemMapper.mapItemToDto(itemAfterSave);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(requestOfItem));
        when(itemRepository.save(item)).thenReturn(itemAfterSave);

        ItemResponseDto createItemResponseDto = itemService.createItem(itemRequestDto, user.getId());

        assertThat(createItemResponseDto.getRequestId(), equalTo(requestOfItem.getId()));
        assertThat(createItemResponseDto, equalTo(itemAfterSaveDto));
        verify(itemRepository).save(any());
        verify(itemRequestRepository).findById(anyLong());
    }

    @Test
    void createItem_whenUserFoundAndItemRequestDtoIsValidAndRequestIdInBodyButRequestNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "keyboard",
                "keyboard description", true, 1L);
        Item item = ItemMapper.mapDtoToItem(itemRequestDto, user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemRequestDto, user.getId()));
        verify(itemRepository, never()).save(item);
        verify(itemRequestRepository).findById(anyLong());
    }

    @Test
    void createItem_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, null, "keyboard description",
                true, 1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemRequestDto, user.getId()));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenUserFoundAndItemFoundAndUpdateByOwnerAndOnlyNameFieldToUpdate_thenSaveUpdatedItem() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, "logitech keyboard",
                null, null, null);
        Item oldItem = new Item(1L, "keyboard", "keyboard description",
                true, user, null);
        Item itemAfterSave = new Item();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(oldItem.getId())).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(itemArgumentCaptor.capture())).thenReturn(itemAfterSave);

        itemService.updateItem(itemRequestDtoToUpdate, oldItem.getId(), user.getId());

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertThat(oldItem.getId(), equalTo(savedItem.getId()));
        assertThat(itemRequestDtoToUpdate.getName(), equalTo(savedItem.getName()));
        assertThat(oldItem.getDescription(), equalTo(savedItem.getDescription()));
        assertThat(oldItem.getIsAvailable(), equalTo(savedItem.getIsAvailable()));
        assertThat(oldItem.getOwner(), equalTo(savedItem.getOwner()));
        assertThat(oldItem.getItemRequest(), equalTo(savedItem.getItemRequest()));
    }

    @Test
    void updateItem_whenUserFoundAndItemFoundAndUpdateByOwnerAndOnlyDescriptionFieldToUpdate_thenSaveUpdatedItem() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null,
                "brand new logitech keyboard", null, null);
        Item oldItem = new Item(1L, "keyboard", "keyboard description",
                true, user, null);
        Item itemAfterSave = new Item();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(oldItem.getId())).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(itemArgumentCaptor.capture())).thenReturn(itemAfterSave);

        itemService.updateItem(itemRequestDtoToUpdate, oldItem.getId(), user.getId());

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertThat(oldItem.getId(), equalTo(savedItem.getId()));
        assertThat(oldItem.getName(), equalTo(savedItem.getName()));
        assertThat(itemRequestDtoToUpdate.getDescription(), equalTo(savedItem.getDescription()));
        assertThat(oldItem.getIsAvailable(), equalTo(savedItem.getIsAvailable()));
        assertThat(oldItem.getOwner(), equalTo(savedItem.getOwner()));
        assertThat(oldItem.getItemRequest(), equalTo(savedItem.getItemRequest()));
    }

    @Test
    void updateItem_whenUserFoundAndItemFoundAndUpdateByOwnerAndOnlyIsAvailableFieldToUpdate_thenSaveUpdatedItem() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null, null,
                false, null);
        Item oldItem = new Item(1L, "keyboard", "keyboard description", true,
                user, null);
        Item itemAfterSave = new Item();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(oldItem.getId())).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(itemArgumentCaptor.capture())).thenReturn(itemAfterSave);

        itemService.updateItem(itemRequestDtoToUpdate, oldItem.getId(), user.getId());

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertThat(oldItem.getId(), equalTo(savedItem.getId()));
        assertThat(oldItem.getName(), equalTo(savedItem.getName()));
        assertThat(oldItem.getDescription(), equalTo(savedItem.getDescription()));
        assertThat(itemRequestDtoToUpdate.getIsAvailable(), equalTo(savedItem.getIsAvailable()));
        assertThat(oldItem.getOwner(), equalTo(savedItem.getOwner()));
        assertThat(oldItem.getItemRequest(), equalTo(savedItem.getItemRequest()));
    }

    @Test
    void updateItem_whenUserNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null, "description",
                null, null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.updateItem(itemRequestDtoToUpdate, 1L, user.getId()));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenItemNotFound_thenNotFoundExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null, "description",
                null, null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.updateItem(itemRequestDtoToUpdate, 1L, user.getId()));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenNoBodyInRequest_thenNoBodyInRequestExceptionThrown() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null, null,
                null, null);
        Item oldItem = new Item(1L, "keyboard", "keyboard description", true,
                user, null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(oldItem.getId())).thenReturn(Optional.of(oldItem));

        assertThrows(NoBodyInRequestException.class, () ->
                itemService.updateItem(itemRequestDtoToUpdate, oldItem.getId(), user.getId()));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenUpdateNotByOwner_thenAccessDeniedExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User user = new User(2L, "Tashiba", "demetris_patchtgc@lodging.hox");
        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, null, null,
                false, null);
        Item oldItem = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(oldItem.getId())).thenReturn(Optional.of(oldItem));

        assertThrows(AccessDeniedException.class, () ->
                itemService.updateItem(itemRequestDtoToUpdate, oldItem.getId(), user.getId()));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemById_whenItemFoundAndInvokedByOwner_thenReturnItemWithNextAndLastBookingsAndComments() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        User booker1 = new User(3L, "Bryan", "ntay_dmusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BookingShort lastBooking = factory.createProjection(BookingShort.class);
        lastBooking.setId(1L);
        lastBooking.setBooker(booker);
        BookingShort nextBooking = factory.createProjection(BookingShort.class);
        nextBooking.setId(2L);
        nextBooking.setBooker(booker1);
        Comment comment = new Comment(1L, "comment", item, booker, LocalDateTime.now().plusHours(3));

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository
                .findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc(any(), any(), any(), any()))
                .thenReturn(null);
        when(bookingRepository.findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(any(), any(), any()))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(any(), any(), any()))
                .thenReturn(nextBooking);
        when(commentRepository.findByItem(item)).thenReturn(List.of(comment));

        ItemResponseDto returnedItem = itemService.getItemById(item.getId(), owner.getId());

        assertThat(returnedItem.getId(), equalTo(item.getId()));
        assertThat(returnedItem.getName(), equalTo(item.getName()));
        assertThat(returnedItem.getDescription(), equalTo(item.getDescription()));
        assertThat(returnedItem.getIsAvailable(), equalTo(item.getIsAvailable()));
        assertThat(returnedItem.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(returnedItem.getLastBooking().getBookerId(), equalTo(lastBooking.getBooker().getId()));
        assertThat(returnedItem.getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(returnedItem.getNextBooking().getBookerId(), equalTo(nextBooking.getBooker().getId()));
        assertThat(returnedItem.getComments().size(), equalTo(1));
        assertThat(returnedItem.getComments().get(0), equalTo(CommentMapper.mapCommentToDto(comment)));
    }

    @Test
    void getItemById_whenItemFoundAndInvokedNotByOwner_thenReturnItemWithoutNextAndLastBookingsAndWithComments() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        User booker1 = new User(3L, "Bryan", "ntay_dmusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BookingShort lastBooking = factory.createProjection(BookingShort.class);
        lastBooking.setId(1L);
        lastBooking.setBooker(booker);
        BookingShort nextBooking = factory.createProjection(BookingShort.class);
        nextBooking.setId(2L);
        nextBooking.setBooker(booker1);
        Comment comment = new Comment(1L, "comment", item, booker, LocalDateTime.now().plusHours(3));

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem(item)).thenReturn(List.of(comment));

        ItemResponseDto returnedItem = itemService.getItemById(item.getId(), booker.getId());

        verify(bookingRepository, never())
                .findFirstByItemAndStartBeforeAndEndAfterAndStatusInOrderByStartAsc(any(), any(), any(), any());
        verify(bookingRepository, never()).findFirstByItemAndEndBeforeAndStatusInOrderByEndDesc(any(), any(), any());
        verify(bookingRepository, never()).findFirstByItemAndStartAfterAndStatusInOrderByStartAsc(any(), any(), any());

        assertThat(returnedItem.getId(), equalTo(item.getId()));
        assertThat(returnedItem.getName(), equalTo(item.getName()));
        assertThat(returnedItem.getDescription(), equalTo(item.getDescription()));
        assertThat(returnedItem.getIsAvailable(), equalTo(item.getIsAvailable()));
        assertThat(returnedItem.getLastBooking(), equalTo(null));
        assertThat(returnedItem.getNextBooking(), equalTo(null));
        assertThat(returnedItem.getComments().size(), equalTo(1));
        assertThat(returnedItem.getComments().get(0), equalTo(CommentMapper.mapCommentToDto(comment)));
    }

    @Test
    void getItemById_whenItemNotFound_thenNotFoundExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(item.getId(), owner.getId()));
    }

    @Test
    void getAllItemsOfOwner_whenUserFound_thenReturnItemsCollectionWithNextAndLastBookingsAndComments() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        User booker1 = new User(3L, "Bryan", "ntay_dmusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        Item item1 = new Item(2L, "table", "table description", true,
                owner, null);
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2),
                item, booker, BookingStatus.APPROVED);
        Booking currentBooking = new Booking(2L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1),
                item1, booker, BookingStatus.WAITING);
        Booking nextBooking = new Booking(3L, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3),
                item, booker1, BookingStatus.WAITING);
        Booking nextBooking1 = new Booking(4L, LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(5),
                item1, booker1, BookingStatus.APPROVED);
        Comment commentForItem = new Comment(1L, "comment", item, booker, LocalDateTime.now().plusHours(6));
        Comment commentForItem1 = new Comment(2L, "Fox biol necklace", item1, booker,
                LocalDateTime.now().plusHours(6));

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any(), any())).thenReturn(new PageImpl<>(List.of(item, item1)));
        when(bookingRepository.findAllLastBookings(anyList(), any(), anySet())).thenReturn(List.of(lastBooking));
        when(bookingRepository.findAllCurrentBookings(anyList(), any(), anySet())).thenReturn(List.of(currentBooking));
        when(bookingRepository.findAllNextBookings(anyList(), any(), anySet()))
                .thenReturn(List.of(nextBooking, nextBooking1));
        when(commentRepository.findByItemIn(anyList())).thenReturn(List.of(commentForItem, commentForItem1));

        Collection<ItemResponseDto> returnedItems = itemService.getAllItemsOfOwner(0, 2, owner.getId());
        List<ItemResponseDto> returnedItemsInList = new ArrayList<>(returnedItems);

        assertThat(returnedItems.size(), equalTo(2));
        assertThat(returnedItemsInList.get(0).getId(), equalTo(item.getId()));
        assertThat(returnedItemsInList.get(1).getId(), equalTo(item1.getId()));
        assertThat(returnedItemsInList.get(0).getName(), equalTo(item.getName()));
        assertThat(returnedItemsInList.get(1).getName(), equalTo(item1.getName()));
        assertThat(returnedItemsInList.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(returnedItemsInList.get(1).getDescription(), equalTo(item1.getDescription()));
        assertThat(returnedItemsInList.get(0).getIsAvailable(), equalTo(item.getIsAvailable()));
        assertThat(returnedItemsInList.get(1).getIsAvailable(), equalTo(item1.getIsAvailable()));
        assertThat(returnedItemsInList.get(0).getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(returnedItemsInList.get(0).getLastBooking().getBookerId(),
                equalTo(lastBooking.getBooker().getId()));
        assertThat(returnedItemsInList.get(1).getLastBooking().getId(), equalTo(currentBooking.getId()));
        assertThat(returnedItemsInList.get(1).getLastBooking().getBookerId(),
                equalTo(currentBooking.getBooker().getId()));
        assertThat(returnedItemsInList.get(0).getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(returnedItemsInList.get(0).getNextBooking().getBookerId(),
                equalTo(nextBooking.getBooker().getId()));
        assertThat(returnedItemsInList.get(1).getNextBooking().getId(), equalTo(nextBooking1.getId()));
        assertThat(returnedItemsInList.get(1).getNextBooking().getBookerId(),
                equalTo(nextBooking1.getBooker().getId()));
        assertThat(returnedItemsInList.get(0).getComments().get(0),
                equalTo(CommentMapper.mapCommentToDto(commentForItem)));
        assertThat(returnedItemsInList.get(1).getComments().get(0),
                equalTo(CommentMapper.mapCommentToDto(commentForItem1)));
    }

    @Test
    void getAllItemsOfOwner_whenUserNotFound_thenNotFoundExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");

        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getAllItemsOfOwner(0, 2, owner.getId()));
    }

    @Test
    void searchItemsByText_whenTextIsNotBlank_thenReturnItemsCollection() {
        String text = "make";
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);

        when(itemRepository.searchItemsByText(anyString(), any())).thenReturn(new PageImpl<>(List.of(item)));

        Collection<ItemResponseDto> returnedItems = itemService.searchItemsByText(text, 0, 2);
        List<ItemResponseDto> returnedItemsInList = new ArrayList<>(returnedItems);

        assertThat(returnedItems.size(), equalTo(1));
        assertThat(returnedItemsInList.get(0).getId(), equalTo(item.getId()));
        assertThat(returnedItemsInList.get(0).getName(), equalTo(item.getName()));
        assertThat(returnedItemsInList.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(returnedItemsInList.get(0).getIsAvailable(), equalTo(item.getIsAvailable()));
    }

    @Test
    void searchItemsByText_whenTextIsBlank_thenReturnEmptyCollection() {
        String text = "";

        Collection<ItemResponseDto> returnedItems = itemService.searchItemsByText(text, 0, 2);

        assertThat(returnedItems, notNullValue());
        assertThat(returnedItems.size(), equalTo(0));
        verify(itemRepository, never()).searchItemsByText(text, PageRequest.of(0, 2));
    }

    @Test
    void createComment_whenUserFoundAndItemFoundAndSuitableBookingsExists_thenSaveComment() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2),
                item, booker, BookingStatus.APPROVED);
        Comment commentForItem = new Comment(1L, "comment", item, booker, LocalDateTime.now().plusHours(6));
        CommentDto commentDtoToSave = new CommentDto(null, "Climb excuse miscellaneous",
                null, null);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemAndBookerAndEndBefore(any(), any(), any())).thenReturn(List.of(lastBooking));
        when(commentRepository.save(commentArgumentCaptor.capture())).thenReturn(commentForItem);

        CommentDto commentDto = itemService.createComment(commentDtoToSave, item.getId(), booker.getId());

        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment savedComment = commentArgumentCaptor.getValue();

        assertThat(commentDtoToSave.getText(), equalTo(savedComment.getText()));
        assertThat(commentDto, notNullValue());
    }

    @Test
    void createComment_whenUserFoundAndItemFoundAndSuitableBookingsNotExists_thenNotAvailableExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        CommentDto commentDtoToSave = new CommentDto(null, "Climb excuse miscellaneous",
                null, null);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemAndBookerAndEndBefore(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(NotAvailableException.class, () ->
                itemService.createComment(commentDtoToSave, item.getId(), booker.getId()));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_whenUserNotFound_thenNotFoundExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        CommentDto commentDtoToSave = new CommentDto(null, "Climb excuse miscellaneous",
                null, null);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.createComment(commentDtoToSave, item.getId(), booker.getId()));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        CommentDto commentDtoToSave = new CommentDto(null, "Climb excuse miscellaneous",
                null, null);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.createComment(commentDtoToSave, item.getId(), booker.getId()));
        verify(commentRepository, never()).save(any());
    }
}