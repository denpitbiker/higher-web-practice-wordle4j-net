package ru.yandex.practicum.client.util;

import java.util.Collection;
import java.util.List;

public class WordUtil {

    public static int wordHasAnyLetter(String word, Collection<Character> letters) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (letters.contains(word.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    public static boolean wordHasAllLetters(String word, Collection<Character> letters) {
        for (Character c : letters) {
            if (!word.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    public static boolean wordHasLettersInPlace(String word, List<Character> okLetters) {
        for (int c = 0; c < word.length(); c++) {
            if (okLetters.get(c) != null && okLetters.get(c) != word.charAt(c)) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeWord(String rawWord) throws NullPointerException {
        if (rawWord == null) {
            throw new NullPointerException("Слово не было передано");
        }
        return rawWord.toLowerCase().replaceAll("ё", "е");
    }
}
