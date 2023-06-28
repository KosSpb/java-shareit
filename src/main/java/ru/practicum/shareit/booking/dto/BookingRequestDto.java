package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.FirstOrder;
import ru.practicum.shareit.validation.SecondOrder;

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@GroupSequence({BookingRequestDto.class, FirstOrder.class, SecondOrder.class})
public class BookingRequestDto {
    @NotNull
    @Positive(message = "ID должно быть положительным числом.")
    private Long itemId;
    @FutureOrPresent(groups = FirstOrder.class, message = "Дата начала бронирования не может быть в прошлом.")
    @NotNull(groups = FirstOrder.class, message = "Дата начала бронирования не может быть null")
    private LocalDateTime start;
    @FutureOrPresent(groups = FirstOrder.class, message = "Дата окончания бронирования не может быть в прошлом.")
    @NotNull(groups = FirstOrder.class, message = "Дата окончания бронирования не может быть null")
    private LocalDateTime end;

    @AssertTrue(groups = SecondOrder.class, message = "Дата окончания бронирования должна быть позже даты начала.")
    private boolean isEndAfterStart() {
        return end.isAfter(start);
    }
}