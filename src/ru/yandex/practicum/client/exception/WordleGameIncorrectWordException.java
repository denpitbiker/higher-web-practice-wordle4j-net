package ru.yandex.practicum.client.exception;

public class WordleGameIncorrectWordException extends WordleGameException {

    public WordleGameIncorrectWordException(String word) {
        super("Некорректный формат слова", word);
    }
}
