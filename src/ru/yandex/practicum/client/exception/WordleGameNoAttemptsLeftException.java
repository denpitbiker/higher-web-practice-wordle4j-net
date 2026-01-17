package ru.yandex.practicum.client.exception;

public class WordleGameNoAttemptsLeftException extends WordleGameException {

    public WordleGameNoAttemptsLeftException() {
        super("Попытки отгадать слово закончились");
    }
}
