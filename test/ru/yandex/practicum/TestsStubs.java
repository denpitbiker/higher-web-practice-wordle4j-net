package ru.yandex.practicum;

import ru.yandex.practicum.common.dto.clientresult.WordleClientResult;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticItem;

public class TestsStubs {
    public static final String WORDS_FILE = "words_ru.txt";
    public static final int WORDS_LENGTH = 5;
    public static final String VALID_CHARS_REGEX = "[А-я]+";
    public static final WordleServerStatisticItem STATISTIC_ITEM_1 = new WordleServerStatisticItem("DEN", 1);
    public static final WordleServerStatisticItem STATISTIC_ITEM_2 = new WordleServerStatisticItem("LOL", 2);
    public static final WordleServerStatisticItem STATISTIC_ITEM_3 = new WordleServerStatisticItem("KEK", 1);
    public static final String USERNAME_1 = "ABOBA";
    public static final int USER_1_ATTEMPTS = 2;
    public static final boolean USER_1_USED_HINTS = false;
    public static final WordleClientResult USER_RESULT_1 = new WordleClientResult(USERNAME_1, USER_1_ATTEMPTS, USER_1_USED_HINTS);
    public static final String INITIAL_STATISTIC_JSON_CONTENT = """
            {
              "startPosition": 0,
              "rating": []
            }
            """;
}
