package ru.yandex.practicum.client.exception;

public class WordleAddWordException extends WordleGameException {

    public WordleAddWordException(String word) {
        super("", word);
    }
}
