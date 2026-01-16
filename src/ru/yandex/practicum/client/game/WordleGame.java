package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.client.exception.*;

import java.util.*;

import static ru.yandex.practicum.client.util.WordUtil.normalizeWord;
import static ru.yandex.practicum.client.util.WordUtil.wordHasLettersInPlace;
import static ru.yandex.practicum.client.util.WordUtil.wordHasAllLetters;
import static ru.yandex.practicum.client.util.WordUtil.wordHasAnyLetter;

public class WordleGame {
    private static final char NOT_IN_WORD_MASK = '-';
    private static final char WRONG_PLACE_MASK = '^';
    private static final char RIGHT_PLACE_MASK = '+';

    private final String TAG = getClass().getSimpleName();
    private final WordleGameState state;
    private final Logger logger;

    public static final int WORD_LENGTH = 5;
    public static final int MAX_STEPS = 6;

    public WordleGame(Logger logger, WordleDictionary dictionary) {
        this.logger = logger;
        this.state = new WordleGameState(dictionary);
        logger.log(TAG, "В словарь игры загружено " + dictionary.size() + " слов");
        reset();
    }

    public String getAnswer() {
        return state.getAnswer();
    }

    public void setAnswer(String answer) throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary {
        logger.log(TAG, "Принудительная установка правильного ответа на: " + answer);
        if (answer == null) return;
        reset();
        String normalizedAnswer = normalizeWord(answer); // we don't need to check there length and else,
                                                         // because we'll just look up for it in the dictionary.
        if(!state.getGameDictionary().contains(normalizedAnswer)) throw new WordleGameWordNotFoundInDictionary(normalizedAnswer);
        state.setAnswer(normalizedAnswer);
        logger.log(TAG, "Принудительный ответ установлен: " + normalizedAnswer);
    }

    public boolean hasUsedHint() {
        return state.hasUsedHint();
    }

    public int getUsedAttempts() {
        return state.getUsedAttempts();
    }

    public WordleGameState.GameProgressState getGameProgressState() {
        return state.getGameProgressState();
    }

    public boolean isEnd() {
        return state.getGameProgressState() == WordleGameState.GameProgressState.NO_ATTEMPTS ||
                state.getGameProgressState() == WordleGameState.GameProgressState.WIN;
    }

    public void reset() throws WordleGameEmptyWordsCollectionException {
        logger.log(TAG, "Перезапуск игры");
        state.reset();
        logger.log(TAG, "Игра перезапущена, новое слово: " + state.getAnswer());
    }

    public String checkWord(String rawCandidate) throws WordleGameNoAttemptsLeftException, WordleGameWrongWordLengthException,
            WordleGameIncorrectWordException, WordleGameWordNotFoundInDictionary {
        logger.log(TAG, "Проверка введенного слова: " + rawCandidate);
        if (rawCandidate == null) throw new WordleGameIncorrectWordException(null);
        throwIfNoAttemptsLeft();
        String candidate = normalizeWord(rawCandidate);
        validateWord(candidate);
        state.increaseUsedAttempts();
        state.setLastWord(candidate);
        if (candidate.equalsIgnoreCase(state.getAnswer())) {
            logger.log(TAG, "Слово отгадано");
            state.setGameProgressState(WordleGameState.GameProgressState.WIN);
            return "+++++ слово отгадано";
        }
        if (state.getUsedAttempts() == MAX_STEPS) {
            logger.log(TAG, "Закончились попытки");
            state.setGameProgressState(WordleGameState.GameProgressState.NO_ATTEMPTS);
        }

        logger.log(TAG, "Слово не отгадано, осталось попыток: " + (MAX_STEPS - state.getUsedAttempts()));
        return getCandidatePattern(candidate);
    }

    public String guessWord() throws WordleGameEmptyWordsCollectionException, WordleGameNoAttemptsLeftException {
        logger.log(TAG, "Поиск подсказки");
        throwIfNoAttemptsLeft();
        state.setHasUsedHint(true);
        WordleDictionary leftWords = state.getLeftWords();
        String candidate = normalizeWord(state.getLastWord());
        String candidatePattern = getCandidatePattern(candidate);
        String guessedWord;
        if (candidatePattern.isEmpty()) {
            guessedWord = leftWords.getRandomWord(true);
            logger.log(TAG, "Пользователь еще не вводил слова, подсказываем первое попавшееся слово: " + guessedWord);
            state.setLastWord(guessedWord);
            return guessedWord;
        }
        for (int c = 0; c < candidate.length(); c++) {
            switch (candidatePattern.charAt(c)) {
                case NOT_IN_WORD_MASK:
                    // обрабатывается отдельно из-за возможного пересечения с WRONG_PLACE_MASK (см. getCandidatePattern)
                    break;
                case WRONG_PLACE_MASK:
                    state.getMaybeLetters().add(candidate.charAt(c));
                    break;
                case RIGHT_PLACE_MASK:
                    state.getOkLetters().set(c, candidate.charAt(c));
                    break;
                default:
                    throw new RuntimeException("Неизвестный символ в паттерне слова: " + candidatePattern.charAt(c));
            }
        }
        Set<Character> allLetters = new HashSet<>(state.getMaybeLetters());
        for (Character c : state.getOkLetters()) {
            if (c != null) {
                allLetters.add(c);
            }
        }
        for (int i = 0; i < candidate.length(); i++) {
            char ch = candidate.charAt(i);
            if (!allLetters.contains(ch)) {
                state.getSkipLetters().add(ch);
            }
        }

        List<String> nextWords = new ArrayList<>();
        for (String word : leftWords.getAll()) {
            if (!state.getSkipLetters().isEmpty() && wordHasAnyLetter(word, state.getSkipLetters()) > 0) { //не содержит неправильных букв
                throwIfEqualToAnswer(word);
                continue;
            }
            if (!allLetters.isEmpty() && !wordHasAllLetters(word, allLetters)) { //содержит правильные буквы по максимуму
                throwIfEqualToAnswer(word);
                continue;
            }
            if (!wordHasLettersInPlace(word, state.getOkLetters())) {
                throwIfEqualToAnswer(word);
                continue;
            }
            nextWords.add(word);
        }
        leftWords.clear();
        leftWords.addAll(nextWords);
        if (nextWords.isEmpty()) {
            throw new WordleEmptyCandidatesException(state.getSkipLetters(), state.getMaybeLetters(), state.getOkLetters());
        }
        guessedWord = leftWords.getRandomWord(true);
        logger.log(TAG, "Найдена подсказка, на основе имеющихся знаний: " + guessedWord);
        state.setLastWord(guessedWord);
        return guessedWord;
    }

    private void throwIfEqualToAnswer(String word) {
        if (word.equals(getAnswer())) {
            throw new RuntimeException("Ошибочно удален верный ответ " + getAnswer());
        }
    }

    private void validateWord(String candidate) throws WordleGameWrongWordLengthException,
            WordleGameIncorrectWordException, WordleGameWordNotFoundInDictionary {
        WordleDictionary gameDictionary = state.getGameDictionary();
        if (candidate.length() != WORD_LENGTH) {
            throw new WordleGameWrongWordLengthException(candidate);
        }
        if (!gameDictionary.isCorrectWord(candidate)) {
            throw new WordleGameIncorrectWordException(candidate);
        }
        if (!gameDictionary.contains(candidate)) {
            throw new WordleGameWordNotFoundInDictionary(candidate);
        }
    }

    private String getCandidatePattern(String candidate) {
        String answer = getAnswer();
        Map<Character, Integer> charsCount = new HashMap<>();
        char[] wordPattern = new char[WORD_LENGTH];
        for (int i = 0; i < answer.length(); i++) {
            char ch = answer.charAt(i);
            if (charsCount.containsKey(ch)) {
                charsCount.put(ch, charsCount.get(ch) + 1);
            } else {
                charsCount.put(ch, 1);
            }
        }
        for (int c = 0; c < candidate.length(); c++) {
            char candidateChar = candidate.charAt(c);
            if (answer.charAt(c) == candidateChar) {
                wordPattern[c] = RIGHT_PLACE_MASK;
                charsCount.put(candidateChar, charsCount.get(candidateChar) - 1);
            } else if (charsCount.getOrDefault(candidateChar, 0) == 0) {
                // В оригинальной wordle подсвечивается только то, количество букв, которое действительно содержится в слове.
                // Например, загадано слово "trail", тогда при вводе "teRra" подсветит только первую букву R!
                wordPattern[c] = NOT_IN_WORD_MASK;
            }
        }
        for (int c = 0; c < candidate.length(); c++) {
            char candidateChar = candidate.charAt(c);
            if (charsCount.getOrDefault(candidateChar, 0) != 0) {
                wordPattern[c] = WRONG_PLACE_MASK;
                charsCount.put(candidateChar, charsCount.get(candidateChar) - 1);
            } else if (wordPattern[c] == 0) {
                wordPattern[c] = NOT_IN_WORD_MASK;
            }
        }

        return String.valueOf(wordPattern);
    }

    private void throwIfNoAttemptsLeft() throws WordleGameNoAttemptsLeftException {
        if (state.getUsedAttempts() == MAX_STEPS) throw new WordleGameNoAttemptsLeftException();
    }
}
