package ru.yandex.practicum.client.exception;

public class WordleGameNoSuchWordException extends WordleGameException {

    public WordleGameNoSuchWordException(String word) {
        super("Слово не существует", word);
    }
}
