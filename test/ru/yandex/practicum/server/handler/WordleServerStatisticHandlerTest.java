package ru.yandex.practicum.server.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.common.ResponseCode;
import ru.yandex.practicum.common.ServerConst;
import ru.yandex.practicum.server.handler.statistic.WordleServerStatisticHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.practicum.TestsStubs.USERNAME_1;
import static ru.yandex.practicum.TestsStubs.USER_RESULT_1;

public class WordleServerStatisticHandlerTest extends WordleServerHandlerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private final HttpResponse.BodyHandler<String> bodyHandler =
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    @Test
    @DisplayName("Получение статистики по известному пользователю")
    public void GET_statistic_knownUser_Success() throws IOException, InterruptedException {
        // given
        URI url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(USER_RESULT_1))).build();
        HttpResponse<String> response = client.send(request, bodyHandler);
        assertEquals(ResponseCode.SUCCESS, response.statusCode(), "Пользователь должен быть добавлен");
        url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH, new AbstractMap.SimpleEntry<>(ServerConst.USERNAME_PARAM_KEY, USERNAME_1));
        request = HttpRequest.newBuilder().uri(url).GET().build();
        // when
        response = client.send(request, bodyHandler);
        client.close();
        // then
        assertEquals(ResponseCode.SUCCESS, response.statusCode(), "Пользователь должен быть найден");
    }

    @Test
    @DisplayName("Добавление статистики по известному пользователю")
    public void POST_statistic_knownUser_Success() throws IOException, InterruptedException {
        // given
        URI url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(USER_RESULT_1), StandardCharsets.UTF_8)).build();
        HttpResponse<String> response = client.send(request, bodyHandler);
        assertEquals(ResponseCode.SUCCESS, response.statusCode(), "Пользователь должен быть добавлен");
        response = client.send(request, bodyHandler);
        assertEquals(ResponseCode.SUCCESS, response.statusCode(), "Пользователь должен быть добавлен");
    }

    @Test
    @DisplayName("Добавление статистики по неизвестному пользователю")
    public void POST_statistic_unknownUser_Success() throws IOException, InterruptedException {
        // given
        URI url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gson.toJson(USER_RESULT_1), StandardCharsets.UTF_8)).build();
        HttpResponse<String> response = client.send(request, bodyHandler);
        assertEquals(ResponseCode.SUCCESS, response.statusCode(), "Пользователь должен быть добавлен");
    }

    @Test
    @DisplayName("Получение статистики по неизвестному пользователю")
    public void GET_statistic_unknownUser_NotFound() throws IOException, InterruptedException {
        // given
        URI url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH, new AbstractMap.SimpleEntry<>(ServerConst.USERNAME_PARAM_KEY, USERNAME_1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        // when
        HttpResponse<String> response = client.send(request, bodyHandler);
        client.close();
        // then
        assertEquals(ResponseCode.NOT_FOUND, response.statusCode(), "Пользователль должен быть не найден");
    }

    @Test
    @DisplayName("Получение статистики без query-параметра username")
    public void GET_statistic_noUsernameQuery_NotAllowed() throws IOException, InterruptedException {
        // given
        URI url = apiUrlBuilder.buildUrl(WordleServerStatisticHandler.PATH, new AbstractMap.SimpleEntry<>(USERNAME_1, USERNAME_1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        // when
        HttpResponse<String> response = client.send(request, bodyHandler);
        client.close();
        // then
        assertEquals(ResponseCode.NOT_ALLOWED, response.statusCode(), "Нельзя не передавать параметр username");
    }
}
