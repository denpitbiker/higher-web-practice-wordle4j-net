package ru.yandex.practicum.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.server.util.QueryParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryParserTest {
    private static final String QUERY = "aaa=bbb&gg=d&aaa=c12";
    private static final String QUERY_KEY_AAA = "aaa";
    private static final int QUERY_KEY_AAA_VALUES_COUNT = 2;
    private static final String QUERY_VALUE_BBB = "bbb";

    @Test
    @DisplayName("Проверка создания QueryParser")
    public void constructor_InitQueryParser_InitNoErrors() {
        // then
        assertDoesNotThrow(() -> new QueryParser(QUERY), "Создание QueryParser не должно завершаться с ошибкой");
    }

    @Test
    @DisplayName("Проверка получения query-параметров")
    public void getParam_GetQueryParam_ValidParam() {
        // given
        QueryParser parser = new QueryParser(QUERY);
        // when
        List<String> paramValues = parser.getParam(QUERY_KEY_AAA);
        // then
        assertEquals(QUERY_KEY_AAA_VALUES_COUNT, paramValues.size(), "Неверное количество значений параметра");
        assertEquals(QUERY_VALUE_BBB, paramValues.getFirst(), "Неверное значение параметра");
    }
}
