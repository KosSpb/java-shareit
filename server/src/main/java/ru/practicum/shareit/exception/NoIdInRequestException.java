package ru.practicum.shareit.exception;

public class NoIdInRequestException extends RuntimeException {
    public NoIdInRequestException(String message) {
        super(message);
    }
}
