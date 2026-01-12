package ru.yandex.practicum.client.exception;

public class WordleGameWrongWordLengthException extends WordleGameException {

    public WordleGameWrongWordLengthException(String word) {
        super("Длина слова не подходит", word);
    }
}
