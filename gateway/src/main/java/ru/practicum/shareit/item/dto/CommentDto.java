package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.FirstOrder;
import ru.practicum.shareit.validation.SecondOrder;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@GroupSequence({CommentDto.class, FirstOrder.class, SecondOrder.class})
public class CommentDto {
    private Long id;
    @NotBlank(groups = FirstOrder.class, message = "Комментарий не может быть пустым.")
    @Size(groups = SecondOrder.class, min = 10, message = "Комментарий должен содержать от 10 символов.")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
