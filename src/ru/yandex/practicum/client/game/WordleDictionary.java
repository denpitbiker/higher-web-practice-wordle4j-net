package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.util.WordUtil;

import java.util.*;

public class WordleDictionary {
    private final List<String> words = new ArrayList<>();
    private final int wordLength;

    private final Random random = new Random();

    public WordleDictionary(int wordLength) {
        this.wordLength = wordLength;
    }

    public Collection<String> getAll() {
        return words;
    }

    public int getWordLength() {
        return wordLength;
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
        return word != null && !word.isBlank() && !word.contains(" ") && word.length() == wordLength;
    }

    public int size() {
        return words.size();
    }

    public void clear() {
        words.clear();
    }

    public String getRandomWord() {
        return getRandomWord(false);
    }

    public String getRandomWord(boolean withRemove) {
        if (isEmpty()) throw new RuntimeException("Пустая коллекция слов для игры");
        int index = random.nextInt(words.size());
        if (withRemove) {
            return words.remove(index);
        } else {
            return words.get(index);
        }
    }
}
