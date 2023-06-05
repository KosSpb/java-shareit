package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.annotation.NullableNotBlank;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ItemDto {
    private Long id;
    @NullableNotBlank(message = "Название вещи не может быть пустым")
    @Size(min = 2, max = 30, message = "Название вещи должно содержать от 2 до 30 символов")
    private String name;
    @NullableNotBlank(message = "Описание вещи вещи не может быть пустым")
    @Size(min = 5, max = 250, message = "Описание вещи должно содержать от 5 до 250 символов")
    private String description;
    @JsonProperty(value = "available")
    private Boolean isAvailable;
    private Integer rentCounter;
}
