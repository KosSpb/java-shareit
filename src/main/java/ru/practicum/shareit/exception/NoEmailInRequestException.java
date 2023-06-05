package ru.practicum.shareit.exception;

public class NoEmailInRequestException extends RuntimeException {
    public NoEmailInRequestException(String message) {
        super(message);
    }
}
