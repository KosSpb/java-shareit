package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
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
class ItemServiceIntegrationTest {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Test
    void updateItem_whenUserFoundAndItemFoundAndUpdateByOwnerAndOnlyNameFieldToUpdate_thenShouldSaveUpdatedItem() {
        User user = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        ItemRequestDto oldItem = new ItemRequestDto(1L, "keyboard",
                "keyboard description", true, null);
        userRepository.save(user);

        ItemResponseDto oldItemAfterSave = itemService.createItem(oldItem, user.getId());

        assertThat(oldItemAfterSave.getId(), equalTo(1L));
        assertThat(oldItemAfterSave.getName(), equalTo(oldItem.getName()));
        assertThat(oldItemAfterSave.getDescription(), equalTo(oldItem.getDescription()));
        assertThat(oldItemAfterSave.getIsAvailable(), equalTo(oldItem.getIsAvailable()));

        ItemRequestDto itemRequestDtoToUpdate = new ItemRequestDto(null, "logitech keyboard",
                null, null, null);
        itemService.updateItem(itemRequestDtoToUpdate, oldItemAfterSave.getId(), user.getId());

        TypedQuery<Item> query =
                entityManager.createQuery("SELECT i FROM Item i WHERE i.name = :name", Item.class);
        Item updatedItemAfterSave =
                query.setParameter("name", itemRequestDtoToUpdate.getName()).getSingleResult();

        assertThat(updatedItemAfterSave.getId(), equalTo(oldItemAfterSave.getId()));
        assertThat(updatedItemAfterSave.getName(), equalTo(itemRequestDtoToUpdate.getName()));
        assertThat(updatedItemAfterSave.getDescription(), equalTo(oldItemAfterSave.getDescription()));
        assertThat(updatedItemAfterSave.getIsAvailable(), equalTo(oldItemAfterSave.getIsAvailable()));
        assertThat(updatedItemAfterSave.getOwner(), equalTo(user));
        assertThat(updatedItemAfterSave.getItemRequest(), nullValue());
    }

    @Test
    void createComment_whenUserFoundAndItemFoundAndSuitableBookingsExists_thenShouldSaveComment() {
        User owner = new User(1L, "Jenevieve", "cannon_leamon786@hearings.jwr");
        User booker = new User(2L, "Quanta", "deontay_deramusn@aspect.pjx");
        Item item = new Item(1L, "keyboard", "keyboard description", true,
                owner, null);
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusHours(9), LocalDateTime.now().minusHours(7),
                item, booker, BookingStatus.APPROVED);
        CommentDto commentDtoToSave = new CommentDto(null, "Climb excuse miscellaneous",
                null, null);
        userRepository.saveAll(List.of(owner, booker));
        itemRepository.save(item);
        bookingRepository.save(lastBooking);

        itemService.createComment(commentDtoToSave, item.getId(), booker.getId());

        TypedQuery<Comment> query =
                entityManager.createQuery("Select c from Comment c where c.text = :text", Comment.class);
        Comment commentAfterSave = query.setParameter("text", commentDtoToSave.getText()).getSingleResult();

        assertThat(commentAfterSave.getId(), equalTo(1L));
        assertThat(commentAfterSave.getText(), equalTo(commentDtoToSave.getText()));
        assertThat(commentAfterSave.getItem(), equalTo(item));
        assertThat(commentAfterSave.getAuthor(), equalTo(booker));
        assertThat(commentAfterSave.getCreated(), notNullValue());
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
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusHours(9), LocalDateTime.now().minusHours(7),
                item, booker, BookingStatus.APPROVED);
        Booking lastBooking1 = new Booking(2L, LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(2),
                item1, booker, BookingStatus.WAITING);
        Booking currentBooking = new Booking(3L, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(3),
                item, booker1, BookingStatus.WAITING);
        Booking nextBooking = new Booking(4L, LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(5),
                item1, booker1, BookingStatus.APPROVED);
        CommentDto commentForItem = new CommentDto(1L, "comment", null, null);
        CommentDto commentForItem1 = new CommentDto(2L, "Fox biol necklace", null, null);
        userRepository.saveAll(List.of(owner, booker, booker1));
        itemRepository.saveAll(List.of(item, item1));
        bookingRepository.saveAll(List.of(lastBooking, lastBooking1, currentBooking, nextBooking));
        CommentDto commentAfterSave = itemService.createComment(commentForItem, item.getId(), booker.getId());
        CommentDto commentAfterSave1 = itemService.createComment(commentForItem1, item1.getId(), booker.getId());

        List<ItemResponseDto> returnedItems =
                new ArrayList<>(itemService.getAllItemsOfOwner(0, 2, owner.getId()));

        TypedQuery<Item> query =
                entityManager.createQuery("SELECT i FROM Item i WHERE i.owner = :owner", Item.class);
        List<Item> itemsFromDb = query.setParameter("owner", owner).getResultList();

        assertThat(2, allOf(equalTo(returnedItems.size()), equalTo(itemsFromDb.size())));
        assertThat(item.getId(), allOf(equalTo(returnedItems.get(0).getId()), equalTo(itemsFromDb.get(0).getId())));
        assertThat(item1.getId(), allOf(equalTo(returnedItems.get(1).getId()), equalTo(itemsFromDb.get(1).getId())));
        assertThat(item.getName(),
                allOf(equalTo(returnedItems.get(0).getName()), equalTo(itemsFromDb.get(0).getName())));
        assertThat(item1.getName(),
                allOf(equalTo(returnedItems.get(1).getName()), equalTo(itemsFromDb.get(1).getName())));
        assertThat(item.getDescription(),
                allOf(equalTo(returnedItems.get(0).getDescription()), equalTo(itemsFromDb.get(0).getDescription())));
        assertThat(item1.getDescription(),
                allOf(equalTo(returnedItems.get(1).getDescription()), equalTo(itemsFromDb.get(1).getDescription())));
        assertThat(item.getIsAvailable(),
                allOf(equalTo(returnedItems.get(0).getIsAvailable()), equalTo(itemsFromDb.get(0).getIsAvailable())));
        assertThat(item1.getIsAvailable(),
                allOf(equalTo(returnedItems.get(1).getIsAvailable()), equalTo(itemsFromDb.get(1).getIsAvailable())));
        assertThat(returnedItems.get(0).getComments().get(0), equalTo(commentAfterSave));
        assertThat(returnedItems.get(1).getComments().get(0), equalTo(commentAfterSave1));
        assertThat(returnedItems.get(0).getLastBooking().getId(), equalTo(currentBooking.getId()));
        assertThat(returnedItems.get(0).getLastBooking().getBookerId(),
                equalTo(currentBooking.getBooker().getId()));
        assertThat(returnedItems.get(1).getLastBooking().getId(), equalTo(lastBooking1.getId()));
        assertThat(returnedItems.get(1).getLastBooking().getBookerId(),
                equalTo(lastBooking1.getBooker().getId()));
        assertThat(returnedItems.get(0).getNextBooking(), nullValue());
        assertThat(returnedItems.get(1).getNextBooking().getId(), equalTo(nextBooking.getId()));
        assertThat(returnedItems.get(1).getNextBooking().getBookerId(),
                equalTo(nextBooking.getBooker().getId()));
        assertThat(owner, allOf(equalTo(itemsFromDb.get(0).getOwner()), equalTo(itemsFromDb.get(1).getOwner())));
        assertThat(itemsFromDb.get(0).getItemRequest(), nullValue());
        assertThat(itemsFromDb.get(1).getItemRequest(), nullValue());
    }
}