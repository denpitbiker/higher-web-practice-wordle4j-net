package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.exception.WordleGameEmptyWordsCollectionException;
import ru.yandex.practicum.client.util.WordUtil;

import java.util.*;

public class WordleDictionary {
    private final List<String> words = new ArrayList<>();
    private final int wordLength;
    private final String validCharsRegex;

    private final Random random = new Random();

    public WordleDictionary(int wordLength, String validCharsRegex) {
        this.wordLength = wordLength;
        this.validCharsRegex = validCharsRegex;
    }

    public Collection<String> getAll() {
        return words;
    }

    public int getWordLength() {
        return wordLength;
    }

    public String getValidCharsRegex() {
        return validCharsRegex;
    }

    public boolean contains(String candidate) {
        return words.contains(candidate);
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public void addAll(Collection<String> words) {
        this.words.addAll(words.stream().filter(this::isCorrectWord).map(WordUtil::normalizeWord).toList());
    }

    public boolean isCorrectWord(String word) {
        return word != null && !word.isBlank() && word.matches(validCharsRegex) && word.length() == wordLength;
    }

    public int size() {
        return words.size();
    }

    public void clear() {
        words.clear();
    }

    public String getRandomWord() throws WordleGameEmptyWordsCollectionException {
        return getRandomWord(false);
    }

    public String getRandomWord(boolean withRemove) throws WordleGameEmptyWordsCollectionException {
        if (isEmpty()) throw new WordleGameEmptyWordsCollectionException();
        int index = random.nextInt(words.size());
        if (withRemove) {
            return words.remove(index);
        } else {
            return words.get(index);
        }
    }
}
