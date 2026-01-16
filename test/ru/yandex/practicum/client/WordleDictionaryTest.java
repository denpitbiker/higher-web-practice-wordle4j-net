package ru.yandex.practicum.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.exception.WordleGameEmptyWordsCollectionException;
import ru.yandex.practicum.client.game.WordleDictionary;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {
    private static final int WORD_LENGTH = 5;
    private static final int VALID_WORDS_COUNT = 2;
    private static final String VALID_REGEX = "[А-ЯЁа-яё]+";

    private static final String WORD_APPLE_RU_UPPER = "ЯБЛОКО"; // 6, не подойдет для len=5
    private static final String WORD_MIXED_CASE_WITH_YO = "ЁжИкА"; // 5, после normalize -> "ежика"
    private static final String WORD_NORMALIZED_WITH_YO = "ежика";

    private static final String WORD_VALID_1 = "арбуз"; // 5
    private static final String WORD_VALID_2 = "вишня"; // 5

    private static final String WORD_INVALID_NULL = null;
    private static final String WORD_INVALID_BLANK = "   ";
    private static final String WORD_INVALID_DIGITS = "аб12в";
    private static final String WORD_INVALID_LATIN = "abcde";
    private static final String WORD_INVALID_SHORT = "кот"; // 3
    private static final String WORD_INVALID_LONG = "машина"; // 6

    private WordleDictionary dict;

    @BeforeEach
    void init() {
        // given
        dict = new WordleDictionary(WORD_LENGTH, VALID_REGEX);
    }

    // ---------- isCorrectWord ----------

    @Test
    @DisplayName("Проверка отсечения слов из пробелов, слишком коротких, слишком длинных, null")
    void isCorrectWord_rejectsNullBlankAndWrongLength() {
        // then
        assertFalse(dict.isCorrectWord(WORD_INVALID_NULL), "Null не может быть корректным словом");
        assertFalse(dict.isCorrectWord(WORD_INVALID_BLANK), "Символы пробела не могут быть корректным словом");
        assertFalse(dict.isCorrectWord(WORD_INVALID_SHORT), "Слишком короткое слово не может быть корректным");
        assertFalse(dict.isCorrectWord(WORD_INVALID_LONG), "Слишком длинное слово не может быть корректным");
    }

    @Test
    @DisplayName("Проверка отсечения слов с цифрами, англ. буквами")
    void isCorrectWord_rejectsNonCyrillicOrWithDigits() {
        // then
        assertFalse(dict.isCorrectWord(WORD_INVALID_DIGITS), "Слово не может содержать цифры");
        assertFalse(dict.isCorrectWord(WORD_INVALID_LATIN), "Слово не может содержать буквы латиницы");
    }

    @Test
    @DisplayName("Проверка, что валидные слова проходят проверку isCorrectWord")
    void isCorrectWord_acceptsCyrillicWordsWithExactLength() {
        // then
        assertTrue(dict.isCorrectWord(WORD_VALID_1), "Отсечено валидное слово арбуз");
        assertTrue(dict.isCorrectWord(WORD_VALID_2), "Отсечено валидное слово вишня");
    }

    // ---------- addAll / normalization / filtering ----------

    @Test
    @DisplayName("Проверка, в словарь добавляются только корректные слова")
    void addAll_addsOnlyCorrectWords_andNormalizes() {
        // when
        dict.addAll(List.of(
                WORD_VALID_1,              // valid
                WORD_INVALID_DIGITS,       // invalid (regex)
                WORD_INVALID_SHORT,        // invalid (len)
                WORD_INVALID_BLANK,        // invalid (blank)
                WORD_MIXED_CASE_WITH_YO    // valid by regex/len, should normalize
        ));
        // then
        assertEquals(VALID_WORDS_COUNT, dict.size());

        // WORD_VALID_1 уже в нижнем регистре, должен быть как есть
        assertTrue(dict.contains(WORD_VALID_1), "Слово арбуз подходит для словаря");

        // "ЁжИкА" должен нормализоваться в "ежика" (toLowerCase + ё->е)
        assertTrue(dict.contains(WORD_NORMALIZED_WITH_YO), "Слово арбуз подходит для ЁжИкА подходит для словаря после нормализации");

        // исходный вариант после normalize не обязан присутствовать
        assertFalse(dict.contains(WORD_MIXED_CASE_WITH_YO), "Слово ЁжИкА не подходит для словаря");
    }

    @Test
    @DisplayName("Проверка, что в словарь не добавится слово неверной длины")
    void addAll_doesNotAddIncorrectLengthEvenIfCyrillic() {
        // when
        dict.addAll(List.of(WORD_APPLE_RU_UPPER)); // "ЯБЛОКО" length 6
        // then
        assertTrue(dict.isEmpty(), "Слово яблоко не должно быть добавлено в словарь");
    }

    // ---------- contains/isEmpty/clear/getAll ----------

    @Test
    @DisplayName("Проверка, что в словарь корректно очищается")
    void contains_isEmpty_size_clear_workCorrectly() {
        // given
        assertTrue(dict.isEmpty(), "Словарь должен быть пуст");
        assertEquals(0, dict.size(), "Словарь должен быть пуст");
        // when
        dict.addAll(List.of(WORD_VALID_1, WORD_VALID_2));
        // then
        assertFalse(dict.isEmpty(), "Словарь должен содержать элементы");
        assertEquals(VALID_WORDS_COUNT, dict.size(), "Словарь должен содержать элементы");
        assertTrue(dict.contains(WORD_VALID_1), "Словарь должен содержать слово арбуз");
        assertTrue(dict.contains(WORD_VALID_2), "Словарь должен содержать вишня");

        dict.clear();
        assertTrue(dict.isEmpty(), "Словарь должен быть очищен");
        assertEquals(0, dict.size(), "Словарь должен быть очищен");
    }

    // ---------- getRandomWord ----------

    @Test
    @DisplayName("Проверка, что если словарь пуст кинется исключение WordleGameEmptyWordsCollectionException")
    void getRandomWord_whenEmpty_throwsException() {
        // then
        assertThrows(WordleGameEmptyWordsCollectionException.class, dict::getRandomWord, "Словарь пуст, ожидалось исключение WordleGameEmptyWordsCollectionException");
        assertThrows(WordleGameEmptyWordsCollectionException.class, () -> dict.getRandomWord(true), "Словарь пуст, ожидалось исключение WordleGameEmptyWordsCollectionException");
    }

    @Test
    @DisplayName("Проверка, что после получения случайного слова (withRemove: false) оно не будет удалено из словаря")
    void getRandomWord_withoutRemove_returnsExistingWord_andDoesNotChangeSize() throws WordleGameEmptyWordsCollectionException {
        // when
        dict.addAll(List.of(WORD_VALID_1, WORD_VALID_2));
        int before = dict.size();
        String randomWord = dict.getRandomWord(false);
        // then
        assertNotNull(randomWord, "Случайное слово не должно быть null");
        assertTrue(dict.contains(randomWord), "Словарь должен содержать полученное случайное слово");
        assertEquals(before, dict.size(), "Размер словаря не должен меняться");
    }

    @Test
    @DisplayName("Проверка, что после получения случайного слова (withRemove: true) оно не будет удалено из словаря")
    void getRandomWord_withRemove_removesReturnedWord_andDecreasesSize() throws WordleGameEmptyWordsCollectionException {
        // when
        dict.addAll(List.of(WORD_VALID_1, WORD_VALID_2));
        int before = dict.size();
        String removed = dict.getRandomWord(true);
        // then
        assertNotNull(removed, "Случайное слово не должно быть null");
        assertEquals(before - 1, dict.size(), "Размер словаря должен уменьшиться на 1");
        assertFalse(dict.contains(removed), "Словарь не должен содержать полученное случайное слово");
    }
}
