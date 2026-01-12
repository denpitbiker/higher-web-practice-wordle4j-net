package ru.yandex.practicum.client.exception;

public class WordleGameWrongWordException extends WordleGameException {

    public WordleGameWrongWordException(String word) {
        super("Неправильное слово", word);
    }
}
