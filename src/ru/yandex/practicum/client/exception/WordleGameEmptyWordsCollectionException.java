package ru.yandex.practicum.client.exception;

public class WordleGameEmptyWordsCollectionException extends RuntimeException {

    public WordleGameEmptyWordsCollectionException() {
        super("Пустая коллекция слов для игры");
    }
}
