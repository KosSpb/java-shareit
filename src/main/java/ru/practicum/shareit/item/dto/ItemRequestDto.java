package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.annotation.NullableNotBlank;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;
    @NotNull(groups = OnCreate.class, message = "Название вещи не может быть null")
    @NullableNotBlank(message = "Название вещи не может быть пустым")
    @Size(min = 2, max = 30, message = "Название вещи должно содержать от 2 до 30 символов")
    private String name;
    @NotNull(groups = OnCreate.class, message = "Описание вещи не может быть null")
    @NullableNotBlank(message = "Описание вещи вещи не может быть пустым")
    @Size(min = 5, max = 250, message = "Описание вещи должно содержать от 5 до 250 символов")
    private String description;
    @JsonProperty(value = "available")
    @NotNull(groups = OnCreate.class, message = "Доступность вещи не может быть null")
    private Boolean isAvailable;
    private Long requestId;
}
