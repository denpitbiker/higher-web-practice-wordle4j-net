package ru.yandex.practicum.client.exception;

public abstract class WordleGameException extends Exception {

    private String word;

    public WordleGameException(String message, String word) {
        this(message);
        this.word = word;
    }

    public WordleGameException(String message) {
        super(message);
    }

    public String getWord() {
        return word;
    }
}
