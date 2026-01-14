package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.client.exception.*;

import java.util.*;

import static ru.yandex.practicum.client.util.WordUtil.normalizeWord;
import static ru.yandex.practicum.client.util.WordUtil.wordHasLettersInPlace;
import static ru.yandex.practicum.client.util.WordUtil.wordHasAllLetters;
import static ru.yandex.practicum.client.util.WordUtil.wordHasAnyLetter;

/*
    в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

    в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

    не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {
    private static final char NOT_IN_WORD_MASK = '-';
    private static final char WRONG_PLACE_MASK = '^';
    private static final char RIGHT_PLACE_MASK = '+';

    private final String TAG = getClass().getSimpleName();
    private final WordleDictionary gameDictionary;
    private final WordleDictionary leftWords;
    private final Set<String> guesses = new HashSet<>();
    private final Logger logger;
    private final Set<Character> skipLetters = new HashSet<>();
    private final Set<Character> maybeLetters = new HashSet<>();
    private final List<Character> okLetters = new ArrayList<>();
    private String answer;
    private int steps = -1;

    public static final int WORD_LENGTH = 5;
    public static final int MAX_STEPS = 6;

    public WordleGame(Logger logger, WordleDictionary dictionary) {
        this.logger = logger;
        this.gameDictionary = dictionary;
        this.leftWords = new WordleDictionary(dictionary.getWordLength());
        logger.log(TAG, "В словарь игры загружено " + gameDictionary.size() + " слов");
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isEnd() {
        return steps <= 0;
    }

    public void reset() {
        logger.log(TAG, "Перезапуск игры");
        steps = MAX_STEPS;
        skipLetters.clear();
        maybeLetters.clear();
        okLetters.clear();
        okLetters.addAll(Arrays.asList(new Character[WORD_LENGTH]));
        leftWords.addAll(gameDictionary.getAll());
        answer = leftWords.getRandomWord();
        logger.log(TAG, "Игра перезапущена, новое слово: " + answer);
    }

    public String checkWord(String rawCandidate) throws WordleGameException {
        logger.log(TAG, "Проверка введенного слова");
        String candidate = normalizeWord(rawCandidate);
        validateWord(candidate);
        steps--;
        if (candidate.equalsIgnoreCase(answer)) {
            logger.log(TAG, "Слово отгадано");
            steps *= -1;
            return "+++++ слово отгадано";
        }

        logger.log(TAG, "Слово не отгадано, осталось попыток: " + steps);
        return makeResume(candidate);
    }

    public String getState() {
        return String.format("попыток %d/%d", Math.abs(steps), MAX_STEPS);
    }

    public String guessWord(String rawCandidate) {
        String candidate = normalizeWord(rawCandidate);
        String candidatePattern = makeResume(candidate);
        if (candidatePattern.isEmpty()) {
            return leftWords.getRandomWord(true);
        }
        for (int c = 0; c < candidate.length(); c++) {
            switch (candidatePattern.charAt(c)) {
                case NOT_IN_WORD_MASK:
                    skipLetters.add(candidate.charAt(c));
                    break;
                case WRONG_PLACE_MASK:
                    maybeLetters.add(candidate.charAt(c));
                    break;
                case RIGHT_PLACE_MASK:
                    okLetters.set(c, candidate.charAt(c));
                    break;
                default:
                    throw new RuntimeException("Неизвестный символ в паттерне слова: " + candidatePattern.charAt(c));
            }
        }
        Set<Character> allLetters = new HashSet<>(maybeLetters);
        for (Character c : okLetters) {
            if (c != null) {
                allLetters.add(c);
            }
        }

        List<String> nextWords = new ArrayList<>();
        for (String word : leftWords.getAll()) {
            if (!skipLetters.isEmpty() && wordHasAnyLetter(word, skipLetters) > 0) { //не содержит неправильных букв
                if (word.equals(answer)) {
                    throw new RuntimeException("Ошибочно удален верный ответ " + answer);
                }
                continue;
            }
            if (!allLetters.isEmpty() && !wordHasAllLetters(word, allLetters)) { //содержит правильные буквы по максимуму
                if (word.equals(answer)) {
                    throw new RuntimeException("Ошибочно удален верный ответ " + answer);
                }
                continue;
            }
            if (!wordHasLettersInPlace(word, okLetters)) {
                if (word.equals(answer)) {
                    throw new RuntimeException("Ошибочно удален верный ответ " + answer);
                }
                continue;
            }
            nextWords.add(word);
        }
        leftWords.clear();
        leftWords.addAll(nextWords);
        if (nextWords.isEmpty()) {
            throw new WordleEmptyCandidatesException(skipLetters, maybeLetters, okLetters);
        }
        return leftWords.getRandomWord(true);
    }

    private void validateWord(String candidate) throws WordleGameException {
        if (candidate.length() != WORD_LENGTH) {
            throw new WordleGameWrongWordLengthException(candidate);
        }
        if (!gameDictionary.isCorrectWord(candidate)) {
            throw new WordleGameWrongWordException(candidate);
        }
        if (!gameDictionary.contains(candidate)) {
            throw new WordleGameNoSuchWordException(candidate);
        }
    }

    private String makeResume(String candidate) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < candidate.length(); c++) {
            if (answer.charAt(c) == candidate.charAt(c)) {
                sb.append(RIGHT_PLACE_MASK);
            } else if (answer.contains(String.valueOf(candidate.charAt(c)))) {
                sb.append(WRONG_PLACE_MASK);
            } else {
                sb.append(NOT_IN_WORD_MASK);
            }
        }
        return sb.toString();
    }
}
