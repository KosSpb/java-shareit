package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CommentMapperTest {

    @Test
    void mapCommentToDto_whenInvoked_thenReturnCommentDto() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        User author = new User(2L, "Hava", "caly_covelll@tribunal.ir");
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, null);
        Comment comment = new Comment(1L, "good comment", item, author, LocalDateTime.now());

        CommentDto commentDto = CommentMapper.mapCommentToDto(comment);

        assertThat(commentDto.getId(), equalTo(comment.getId()));
        assertThat(commentDto.getText(), equalTo(comment.getText()));
        assertThat(commentDto.getAuthorName(), equalTo(author.getName()));
        assertThat(commentDto.getCreated(), equalTo(comment.getCreated()));
    }

    @Test
    void mapDtoToComment_whenInvoked_thenReturnComment() {
        User owner = new User(1L, "Darus", "kenika_plummeryps@networks.or");
        User author = new User(2L, "Hava", "caly_covelll@tribunal.ir");
        Item item = new Item(1L, "spoon", "steel spoon", true, owner, null);
        CommentDto commentDto = new CommentDto(1L, "good comment", author.getName(), LocalDateTime.now());

        Comment comment = CommentMapper.mapDtoToComment(commentDto, item, author);

        assertThat(comment.getText(), equalTo(commentDto.getText()));
        assertThat(comment.getItem(), equalTo(item));
        assertThat(comment.getAuthor(), equalTo(author));
    }
}