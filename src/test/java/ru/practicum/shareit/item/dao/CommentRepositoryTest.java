package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(value = {"classpath:testDataAfterMethod.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CommentRepositoryTest {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private User author1;
    private User author2;

    private User owner;
    private LocalDateTime currentTime = LocalDateTime.now();
    private Item item1;
    private Item item2;
    private Comment comment1;
    private Comment comment2;
    private Comment comment3;
    private Comment comment4;


    @BeforeEach
    void addData() {
        owner = new User(null, "Darus", "kenika_plummeryps@networks.or");
        author1 = new User(null, "Mayer", "dearon_rourkeynz2@chevy.gp");
        author2 = new User(null, "Hava", "caly_covelll@tribunal.ir");
        item1 = new Item(null, "spoon", "steel spoon", true, owner, null);
        item2 = new Item(null, "guitar", "wooden guitar", true, owner, null);
        comment1 = new Comment(null, "Отличная вещь! Спасибо владельцу!", item1, author1, currentTime);
        comment2 = new Comment(null, "Вот это гитара! Одолжу ещё разок точно :)", item2, author1, currentTime);
        comment3 = new Comment(null, "Ложка просто супер! Ещё и из нержавейки!", item1, author2, currentTime);
        comment4 = new Comment(null, "На одном из соло лопнула струна. Не рекомендую.", item2, author2,
                currentTime);

        userRepository.saveAll(List.of(owner, author1, author2));
        itemRepository.saveAll(List.of(item1, item2));
        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4));
    }

    @Test
    void idGenerationByDbForCommentEntity() {
        Supplier<NotFoundException> exception = () -> new NotFoundException("Failure of id generation test.");

        Comment foundComment1 = commentRepository.findById(1L).orElseThrow(exception);
        Comment foundComment2 = commentRepository.findById(2L).orElseThrow(exception);
        Comment foundComment3 = commentRepository.findById(3L).orElseThrow(exception);
        Comment foundComment4 = commentRepository.findById(4L).orElseThrow(exception);

        assertThat(foundComment1, equalTo(comment1));
        assertThat(foundComment2, equalTo(comment2));
        assertThat(foundComment3, equalTo(comment3));
        assertThat(foundComment4, equalTo(comment4));
    }

    @Test
    void findByItem() {
        List<Comment> foundComments = commentRepository.findByItem(item2);

        assertThat(foundComments.size(), equalTo(2));
        assertThat(foundComments, allOf(hasItem(comment2), hasItem(comment4)));
    }

    @Test
    void findByItemIn() {
        List<Comment> foundComments = commentRepository.findByItemIn(List.of(item1, item2));

        assertThat(foundComments.size(), equalTo(4));
        assertThat(foundComments, allOf(hasItem(comment2), hasItem(comment4), hasItem(comment1), hasItem(comment3)));
    }
}