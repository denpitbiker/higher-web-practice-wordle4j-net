package ru.yandex.practicum.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.network.ApiUrlBuilder;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiUrlBuilderTest {

    // --- Strings / routes ---
    private static final String IP_LOCALHOST = "127.0.0.1";
    private static final String PORT_8080 = "8080";
    private static final String HOST_HTTP_LOCALHOST_8080 = "http://127.0.0.1:8080";

    private static final String ROUTE_PING = "/ping";
    private static final String ROUTE_SEARCH = "/search";

    // --- Query keys/values ---
    private static final String KEY_Q = "q";
    private static final String VALUE_HELLO_WORLD = "hello world";

    private static final String KEY_PAGE = "page";
    private static final String VALUE_1 = "1";
    private static final String KEY_PAGE_VALUE_1_ENCODED = "page%3D1";

    // --- Expected encoded parts (учитываем, что кодируется ВСЯ query-строка) ---
    private static final String ENCODED_Q_EQ_HELLO_WORLD = "q%3Dhello+world";
    private static final String ENCODED_Q_EQ_HELLO_WORLD_AMP_PAGE_EQ_1 = "q%3Dhello+world%26page%3D1";

    private static final String URI_PREFIX = "http";
    private static final String QUERY_BEGIN = "?";

    @Test
    @DisplayName("Проверка, что ApiUrlBuilder корректно создает схему, порт и ip-адрес")
    void constructor_withIpAndPort_buildsHttpHost() {
        // given
        ApiUrlBuilder builder = new ApiUrlBuilder(IP_LOCALHOST, PORT_8080);
        // when
        URI uri = builder.buildUrl(ROUTE_PING, Map.entry(KEY_PAGE, VALUE_1));
        // then
        assertEquals(URI_PREFIX, uri.getScheme(), "Схема не верна");
        assertEquals(IP_LOCALHOST, uri.getHost(), "Неверный хост");
        assertEquals(Integer.parseInt(PORT_8080), uri.getPort(), "Неверный порт");
    }

    @Test
    @DisplayName("Проверка, что ApiUrlBuilder не переделывает хост, если он задан явно")
    void constructor_withHost_usesHostAsIs() {
        // given
        ApiUrlBuilder builder = new ApiUrlBuilder(HOST_HTTP_LOCALHOST_8080);
        // when
        URI uri = builder.buildUrl(ROUTE_PING, Map.entry(KEY_PAGE, VALUE_1));
        // then
        assertEquals(HOST_HTTP_LOCALHOST_8080 + ROUTE_PING + QUERY_BEGIN + KEY_PAGE_VALUE_1_ENCODED, uri.toString(), "Хосты отличаются");
    }

    @Test
    @DisplayName("Проверка, что ApiUrlBuilder кодирует всю query-строку")
    void buildUrl_withSingleQueryParam_encodesWholeQueryString() {
        // given
        ApiUrlBuilder builder = new ApiUrlBuilder(IP_LOCALHOST, PORT_8080);
        String expected = HOST_HTTP_LOCALHOST_8080 + ROUTE_SEARCH + QUERY_BEGIN + ENCODED_Q_EQ_HELLO_WORLD;
        // when
        URI uri = builder.buildUrl(ROUTE_SEARCH, Map.entry(KEY_Q, VALUE_HELLO_WORLD));
        // then
        assertEquals(expected, uri.toString(), "Не вся query закодирована");
    }

    @Test
    @DisplayName("Проверка, что ApiUrlBuilder сохраняет порядок при кодировании")
    void buildUrl_withMultipleQueryParams_encodesAndKeepsOrder() {
        // given
        ApiUrlBuilder builder = new ApiUrlBuilder(IP_LOCALHOST, PORT_8080);
        String expected = HOST_HTTP_LOCALHOST_8080 + ROUTE_SEARCH + QUERY_BEGIN + ENCODED_Q_EQ_HELLO_WORLD_AMP_PAGE_EQ_1;
        // when
        URI uri = builder.buildUrl(ROUTE_SEARCH, Map.entry(KEY_Q, VALUE_HELLO_WORLD), Map.entry(KEY_PAGE, VALUE_1));
        // then
        assertEquals(expected, uri.toString(), "Порядок query параметров нарушился");
    }

    @Test
    @DisplayName("Проверка, что ApiUrlBuilder не добавляет знак вопроса, если query-параметры не заданы")
    void buildUrl_withNoQueryParams_stillAddsQuestionMarkAndEmptyQuery() {
        // given
        ApiUrlBuilder builder = new ApiUrlBuilder(IP_LOCALHOST, PORT_8080);
        String expected = HOST_HTTP_LOCALHOST_8080 + ROUTE_PING;
        // when
        URI uri = builder.buildUrl(ROUTE_PING /* no params */);
        // then
        assertEquals(expected, uri.toString(), "Лишний знак вопроса");
    }
}
