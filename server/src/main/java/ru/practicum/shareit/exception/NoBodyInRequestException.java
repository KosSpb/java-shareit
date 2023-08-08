package ru.practicum.shareit.exception;

public class NoBodyInRequestException extends RuntimeException {
    public NoBodyInRequestException(String message) {
        super(message);
    }
}
