package ru.yandex.practicum.client.exception;

public class WordleGameWordNotFoundInDictionary extends WordleGameException {

    public WordleGameWordNotFoundInDictionary(String word) {
        super("Слово не существует", word);
    }
}
