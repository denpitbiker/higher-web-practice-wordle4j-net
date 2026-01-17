package ru.yandex.practicum.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.util.WordUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordUtilTest {

    // --- Magic numbers ---
    private static final int XYZ_COUNT_IN_ABC = 0;
    private static final int A_COUNT_IN_ABA = 2;
    private static final int A_AND_B_COUNT_IN_ABACA = 4;

    // --- Test words / strings ---
    private static final String WORD_ABC = "abc";
    private static final String WORD_ABA = "aba";
    private static final String WORD_ABACA = "abaca";
    private static final String WORD_WORLD = "world";
    private static final String WORD_ABC_UPPER_FIRST = "Abc";
    private static final String WORD_ABCD = "abcd";

    private static final String WORD_HELLO_MIXED = "HeLLo";
    private static final String WORD_HELLO_LOWER = "hello";

    private static final String WORD_ELKA_UPPER = "ЁЛКА";
    private static final String WORD_ELKA_LOWER = "елка";
    private static final String WORD_HEDGEHOG_WITH_YO = "ёжик";
    private static final String WORD_HEDGEHOG_NORMALIZED = "ежик";

    private static final String WORD_GREETING_MIXED = "ПрИвЕт-МИР!";
    private static final String WORD_GREETING_LOWER = "привет-мир!";

    // --- Letters ---
    private static final char LETTER_A = 'a';
    private static final char LETTER_B = 'b';
    private static final char LETTER_C = 'c';
    private static final char LETTER_D = 'd';
    private static final char LETTER_O = 'o';
    private static final char LETTER_W = 'w';
    private static final char LETTER_X = 'x';
    private static final char LETTER_Y = 'y';
    private static final char LETTER_Z = 'z';

    // -------- wordHasAnyLetter --------

    @Test
    @DisplayName("Проверка, что если переданы буквы не из слова, то wordHasAnyLetter вернет 0")
    void wordHasAnyLetter_whenNoLettersMatch_returns0() {
        // when
        int count = WordUtil.wordHasAnyLetter(WORD_ABC, Set.of(LETTER_X, LETTER_Y, LETTER_Z));
        // then
        assertEquals(XYZ_COUNT_IN_ABC, count, "XYZ не содержатся в строке ABC");
    }

    @Test
    @DisplayName("Проверка, что если часть букв содержится в слове, то wordHasAnyLetter вернет их количество")
    void wordHasAnyLetter_countsAllOccurrencesInWord() {
        // В WORD_ABACA: 'a' встречается 3 раза, 'b' 1 раз => 4
        // when
        int count = WordUtil.wordHasAnyLetter(WORD_ABACA, Set.of(LETTER_A, LETTER_B));
        // then
        assertEquals(A_AND_B_COUNT_IN_ABACA, count, "AB содержатся в строке ABACA");
    }

    @Test
    @DisplayName("Проверка, что если переданы повторяющиеся буквы, то wordHasAnyLetter не посчитает одну букву несколько раз")
    void wordHasAnyLetter_whenLettersCollectionHasDuplicates_stillCountsByWordPositions() {
        // when
        int count = WordUtil.wordHasAnyLetter(WORD_ABA, List.of(LETTER_A, LETTER_A));
        // then
        assertEquals(A_COUNT_IN_ABA, count, "A содержится в строке ABA");
    }

    // -------- wordHasAllLetters --------

    @Test
    @DisplayName("Проверка, что если все переданные буквы содержатся в слове, то wordHasAllLetters вернет true")
    void wordHasAllLetters_whenAllPresent_returnsTrue() {
        // when
        boolean ok = WordUtil.wordHasAllLetters(WORD_WORLD, Set.of(LETTER_W, LETTER_O, LETTER_D));
        //then
        assertTrue(ok, "Все буквы содержатся в слове");
    }

    @Test
    @DisplayName("Проверка, что если не все переданные буквы содержатся в слове, то wordHasAllLetters вернет false")
    void wordHasAllLetters_whenAnyMissing_returnsFalse() {
        // when
        boolean ok = WordUtil.wordHasAllLetters(WORD_WORLD, Set.of(LETTER_W, LETTER_X));
        // then
        assertFalse(ok, "Не все буквы содержатся в слове");
    }

    @Test
    @DisplayName("Проверка, что если передан пустой массив букв, то wordHasAllLetters вернет true")
    void wordHasAllLetters_whenLettersEmpty_returnsTrue() {
        // when
        boolean ok = WordUtil.wordHasAllLetters(WORD_WORLD, List.of());
        // then
        assertTrue(ok, "Для пустого массива должно быть true");
    }

    @Test
    @DisplayName("Проверка, что нормализация влияет на результат wordHasAllLetters")
    void wordHasAllLetters_isCaseSensitiveUnlessNormalized() {
        // when
        String normalized = WordUtil.normalizeWord(WORD_ABC_UPPER_FIRST);
        // then
        assertTrue(WordUtil.wordHasAllLetters(normalized, Set.of(LETTER_A)), "Abc после нормализации содержит маленькую а");
    }

    // -------- wordHasLettersInPlace --------

    @Test
    @DisplayName("Проверка, что если передан массив null-ов, то wordHasLettersInPlace вернет true")
    void wordHasLettersInPlace_whenAllNullConstraints_returnsTrue() {
        // then
        assertTrue(WordUtil.wordHasLettersInPlace(WORD_ABC, Arrays.asList(null, null, null)), "Ожидалось true тк массив null-ов");
    }

    @Test
    @DisplayName("Проверка, что если буквы находятся на своих местах, то wordHasLettersInPlace вернет true")
    void wordHasLettersInPlace_whenMatchesSpecifiedPositions_returnsTrue() {
        // then
        assertTrue(WordUtil.wordHasLettersInPlace(WORD_ABC, Arrays.asList(LETTER_A, null, LETTER_C)), "Ожидалось true тк буквы на своих местах");
    }

    @Test
    @DisplayName("Проверка, что если буквы находятся не на своих местах, то wordHasLettersInPlace вернет false")
    void wordHasLettersInPlace_whenAnySpecifiedPositionMismatches_returnsFalse() {
        // then
        assertFalse(WordUtil.wordHasLettersInPlace(WORD_ABC, Arrays.asList(LETTER_A, null, LETTER_D)), "Ожидалось false тк буквы не на своих местах");
    }

    @Test
    @DisplayName("Проверка, что кидается IndexOutOfBoundsException если (каким-то чудом) длина массива ОК-букв не совпадает с длиной слова")
    void wordHasLettersInPlace_whenOkLettersShorterThanWord_throwsIndexOutOfBounds() {
        // then
        assertThrows(IndexOutOfBoundsException.class,
                () -> WordUtil.wordHasLettersInPlace(WORD_ABCD, Arrays.asList(LETTER_A, null)), "Ожидалась ошибка IndexOutOfBoundsException");
    }

    // -------- normalizeWord --------

    @Test
    @DisplayName("Проверка нормализации заглавных букв")
    void normalizeWord_lowercases() {
        // then
        assertEquals(WORD_HELLO_LOWER, WordUtil.normalizeWord(WORD_HELLO_MIXED), "Неверный результат нормализации HeLLo");
    }

    @Test
    @DisplayName("Проверка нормализации с буквой ё")
    void normalizeWord_replacesYoWithE() {
        // then
        assertEquals(WORD_ELKA_LOWER, WordUtil.normalizeWord(WORD_ELKA_UPPER), "Неверный результат нормализации слова ЁЛКА");
        assertEquals(WORD_HEDGEHOG_NORMALIZED, WordUtil.normalizeWord(WORD_HEDGEHOG_WITH_YO), "Неверный результат нормализации слова ёжик");
    }

    @Test
    @DisplayName("Проверка, что при нормализации не изменяются правильные символы")
    void normalizeWord_doesNotChangeOtherCharacters() {
        // then
        assertEquals(WORD_GREETING_LOWER, WordUtil.normalizeWord(WORD_GREETING_MIXED), "Неверный результат нормализации ПрИвЕт-МИР!");
    }
}