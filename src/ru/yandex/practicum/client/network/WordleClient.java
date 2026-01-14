package ru.yandex.practicum.client.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.common.ResponseCode;
import ru.yandex.practicum.common.dto.clientresult.WordleClientResult;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticTypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WordleClient {
    private final String TAG = getClass().getSimpleName();
    private final HttpClient httpClient;
    private final Logger logger;
    private final ApiUrlBuilder apiUrlBuilder;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final HttpResponse.BodyHandler<String> bodyHandler =
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    private static final String STATISTICS_ROUTE = "/api/statistic";
    private static final String USERNAME_PARAM_KEY = "username";

    public WordleClient(Logger logger, String ip, int port) {
        this.logger = logger;
        this.apiUrlBuilder = new ApiUrlBuilder(ip, String.valueOf(port));
        httpClient = HttpClient.newHttpClient();
        logger.log(TAG, "Создан http-клиент Wordle");
    }

    public void sendResult(WordleClientResult result) throws IOException, InterruptedException {
        logger.log(TAG, "Обновление статистики пользователя: " + result.getUsername());
        String body = gson.toJson(result);
        URI resultURI = apiUrlBuilder.buildUrl(STATISTICS_ROUTE);
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(resultURI)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, bodyHandler);
        logger.log(TAG, "Получен результат обновления статистики пользователя, код: " + httpResponse.statusCode());
        if (httpResponse.statusCode() != ResponseCode.SUCCESS) {
            throw new RuntimeException("Failed : HTTP error code : " + httpResponse.statusCode());
        }
    }

    public WordleServerStatistic getStatistic(String username) throws IOException, InterruptedException {
        logger.log(TAG, "Запрос статистики для пользователя: " + username);
        URI uri = apiUrlBuilder.buildUrl(STATISTICS_ROUTE, new AbstractMap.SimpleEntry<>(USERNAME_PARAM_KEY, username));
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, bodyHandler);
        logger.log(TAG, "Получена статистика по пользователю, код: " + httpResponse.statusCode());
        if (httpResponse.statusCode() == ResponseCode.SUCCESS) {
            return gson.fromJson(httpResponse.body(), new WordleServerStatisticTypeToken().getType());
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + httpResponse.statusCode());
        }
    }
}
