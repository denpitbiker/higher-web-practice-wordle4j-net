package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.exception.WordleGameEmptyWordsCollectionException;

import java.util.*;

public class WordleGameState {
    private String answer;
    private String lastWord = "";
    private int usedAttempts;
    private boolean hasUsedHint;
    private GameProgressState gameProgressState;
    private final WordleDictionary gameDictionary;
    private final WordleDictionary leftWords;
    private final Set<Character> skipLetters = new HashSet<>();
    private final Set<Character> maybeLetters = new HashSet<>();
    private final List<Character> okLetters = new ArrayList<>();

    public WordleGameState(WordleDictionary dictionary) {
        gameDictionary = dictionary;
        leftWords = new WordleDictionary(dictionary.getWordLength(), dictionary.getValidCharsRegex());
    }

    public void reset() throws WordleGameEmptyWordsCollectionException {
        lastWord = "";
        usedAttempts = 0;
        hasUsedHint = false;
        gameProgressState = GameProgressState.PLAY;
        leftWords.clear();
        skipLetters.clear();
        maybeLetters.clear();
        okLetters.clear();
        okLetters.addAll(Arrays.asList(new Character[gameDictionary.getWordLength()]));
        leftWords.addAll(gameDictionary.getAll());
        answer = leftWords.getRandomWord();
    }

    public WordleDictionary getGameDictionary() {
        return gameDictionary;
    }

    public WordleDictionary getLeftWords() {
        return leftWords;
    }

    public Set<Character> getSkipLetters() {
        return skipLetters;
    }

    public Set<Character> getMaybeLetters() {
        return maybeLetters;
    }

    public List<Character> getOkLetters() {
        return okLetters;
    }

    public GameProgressState getGameProgressState() {
        return gameProgressState;
    }

    public int getUsedAttempts() {
        return usedAttempts;
    }

    public String getLastWord() {
        return lastWord;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean hasUsedHint() {
        return hasUsedHint;
    }

    public void setGameProgressState(GameProgressState gameProgressState) {
        this.gameProgressState = gameProgressState;
    }

    public void setHasUsedHint(boolean hasUsedHint) {
        this.hasUsedHint = hasUsedHint;
    }

    public void increaseUsedAttempts() {
        this.usedAttempts++;
    }

    public void setLastWord(String lastWord) {
        this.lastWord = lastWord;
    }

    public enum GameProgressState {
        PLAY, NO_ATTEMPTS, WIN;
    }
}
