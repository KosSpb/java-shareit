package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.FirstOrder;
import ru.practicum.shareit.validation.SecondOrder;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@GroupSequence({ItemRequestDtoOfRequest.class, FirstOrder.class, SecondOrder.class})
public class ItemRequestDtoOfRequest {
    @NotBlank(groups = FirstOrder.class, message = "Описание запроса не может быть пустым.")
    @Size(groups = SecondOrder.class, min = 20, message = "Описание запроса должно содержать от 20 символов.")
    private String description;
}
