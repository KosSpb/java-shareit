package ru.practicum.shareit.exception;

public class AlreadyDoneException extends RuntimeException {
    public AlreadyDoneException(String message) {
        super(message);
    }
}
