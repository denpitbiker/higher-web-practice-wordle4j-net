package ru.yandex.practicum.client.exception;

import java.util.Collection;

public class WordleEmptyCandidatesException extends RuntimeException {

    private final String okLetters;

    private final String skipLetters;

    private final String maybeLetters;


    public WordleEmptyCandidatesException(Collection<Character> skipLetters, Collection<Character> maybeLetters, Collection<Character> okLetters) {
        super("Не найдено подходящее слово для заданных условий");
        this.okLetters = join(okLetters);
        this.skipLetters = join(skipLetters);
        this.maybeLetters = join(maybeLetters);
    }

    private static String join(Collection<Character> chars) {
        StringBuilder sb = new StringBuilder();
        for (Character c : chars) {
            if (c == null) {
                sb.append("_");
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return String.format("%s, статус: %s, кандидаты: %s, отброшены: %s", super.getMessage(), okLetters, maybeLetters, skipLetters);
    }
}
