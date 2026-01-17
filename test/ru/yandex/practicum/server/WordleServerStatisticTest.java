package ru.yandex.practicum.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.TestsStubs;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WordleServerStatisticTest {
    private List<WordleServerStatisticItem> items;
    private static final String STUB_USERNAME = "STUB";
    private static final int USER_RATING_SHOW_LIMIT = 10;
    private static final int STUB_USER_TOP_2_INDEX = 1;
    private static final int STUB_USER_NOT_IN_TOP_INDEX = 17;
    private static final int STUB_USER_STATISTICS_ITEMS_COUNT = 20;

    @BeforeEach
    public void initItems() {
        items = new ArrayList<>();
        items.add(TestsStubs.STATISTIC_ITEM_1.clone());
        items.add(TestsStubs.STATISTIC_ITEM_2.clone());
        items.add(TestsStubs.STATISTIC_ITEM_3.clone());
    }

    @Test
    @DisplayName("Проверка создания WordleServerStatistic")
    public void constructor_InitWordleServerStatistic_InitNoErrors() {
        // then
        assertDoesNotThrow(() -> new WordleServerStatistic(items), "Создание WordleServerStatistic не должно завершаться с ошибкой");
    }

    @Test
    @DisplayName("Проверка добавления новой победы у существующего пользователя")
    public void incrementUserStatistic_IncrementExistingUserStatistic_IncrementedCount() {
        // given
        WordleServerStatistic statistic = new WordleServerStatistic(items);
        // when
        WordleServerStatisticItem updatedItem = statistic.incrementUserStatistic(TestsStubs.STATISTIC_ITEM_2.getUsername());
        // then
        assertEquals(TestsStubs.STATISTIC_ITEM_2.getCount() + 1, updatedItem.getCount(), "Счетчик побед должен был увеличиться на единицу");
    }

    @Test
    @DisplayName("Проверка добавления новой победы у нового пользователя")
    public void incrementUserStatistic_AddNewUserStatistic_UserAdded() {
        // given
        WordleServerStatistic statistic = new WordleServerStatistic(items);
        // when
        WordleServerStatisticItem updatedItem = statistic.incrementUserStatistic(STUB_USERNAME);
        boolean isAddedToStatistic = statistic.getRating().stream().anyMatch(item -> item.getUsername().equals(STUB_USERNAME));
        // then
        assertEquals(1, updatedItem.getCount(), "Счетчик побед должен быть равен 1");
        assertTrue(isAddedToStatistic, "Новый пользователь должен быть добавлен в статистику");
    }

    @Test
    @DisplayName("Проверка получения статистики несуществующего пользователя")
    public void getUserStatistic_GetStatisticForNonExistingUser_EmptyOptional() {
        // given
        WordleServerStatistic statistic = new WordleServerStatistic(items);
        // when
        Optional<WordleServerStatistic> userStatistic = statistic.getUserStatistic(STUB_USERNAME, USER_RATING_SHOW_LIMIT);
        // then
        assertTrue(userStatistic.isEmpty(), "Новый пользователь не должен был учитываться в статистике");
    }

    @Test
    @DisplayName("Проверка получения статистики существующего пользователя (топ 10)")
    public void getUserStatistic_GetStatisticForExistingUserTop10_CorrectUserStatistic() {
        // given
        generateItemsForGetStatisticForExistingUserTest();
        WordleServerStatistic statistic = new WordleServerStatistic(items);
        String expectedUsername = statistic.getRating().get(STUB_USER_TOP_2_INDEX).getUsername();
        // when
        Optional<WordleServerStatistic> userStatistic = statistic.getUserStatistic(expectedUsername, USER_RATING_SHOW_LIMIT);
        assertTrue(userStatistic.isPresent(), "Должна была вернуться статистика по пользователю");
        assertEquals(USER_RATING_SHOW_LIMIT, userStatistic.get().getRating().size(), "Количество отображаемых пользователей не равно 10");
        String usernameAtSecondPosition = userStatistic.get().getRating().get(STUB_USER_TOP_2_INDEX).getUsername();
        // then
        assertEquals(expectedUsername, usernameAtSecondPosition, "Пользователь находится на неправильной позиции");
    }

    @Test
    @DisplayName("Проверка получения статистики существующего пользователя (не топ 10)")
    public void getUserStatistic_GetStatisticForExistingUserNonTop10_CorrectUserStatistic() {
        // given
        generateItemsForGetStatisticForExistingUserTest();
        WordleServerStatistic statistic = new WordleServerStatistic(items);
        String expectedUsername = statistic.getRating().get(STUB_USER_NOT_IN_TOP_INDEX).getUsername();
        // when
        Optional<WordleServerStatistic> userStatistic = statistic.getUserStatistic(expectedUsername, USER_RATING_SHOW_LIMIT);
        assertTrue(userStatistic.isPresent(), "Должна была вернуться статистика по пользователю");
        assertEquals(STUB_USER_STATISTICS_ITEMS_COUNT - STUB_USER_NOT_IN_TOP_INDEX, userStatistic.get().getRating().size(), "Количество отображаемых пользователей не совпадает с ожидаемым значением");
        String usernameAtFirstPosition = userStatistic.get().getRating().getFirst().getUsername();
        // then
        assertEquals(expectedUsername, usernameAtFirstPosition, "Пользователь находится на неправильной позиции");
    }

    private void generateItemsForGetStatisticForExistingUserTest() {
        items = new ArrayList<>();
        for (int i = STUB_USER_STATISTICS_ITEMS_COUNT; i > 0; i--) {
            items.add(new WordleServerStatisticItem(String.valueOf(i), i));
        }
    }
}
