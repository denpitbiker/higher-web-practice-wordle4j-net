package ru.yandex.practicum.server.handler.statistic;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.common.RequestMethod;
import ru.yandex.practicum.common.dto.clientresult.WordleClientResult;
import ru.yandex.practicum.common.dto.clientresult.WordleServerClientResultTypeToken;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticItem;
import ru.yandex.practicum.server.handler.BaseHttpHandler;
import ru.yandex.practicum.server.repository.WordleServerStatisticRepository;
import ru.yandex.practicum.server.util.QueryParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class WordleServerStatisticHandler extends BaseHttpHandler {
    public static final String DB_JSON = "stats.json";
    public static final String PATH = "/api/statistic";

    private final WordleServerStatisticRepository loader;
    private final Gson gson;
    private static final String USERNAME_PARAM_KEY = "username";
    private static final int USER_RATING_BEFORE_COUNT = 5;
    private static final int USER_RATING_AFTER_COUNT = 5;
    private final WordleServerStatistic statisticsCache;

    public WordleServerStatisticHandler(Gson gson) throws IOException {
        this.loader = new WordleServerStatisticRepository(gson, DB_JSON);
        this.gson = gson;
        this.statisticsCache = loader.load();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case RequestMethod.POST:
                handlePostStatistics(exchange);
                break;
            case RequestMethod.GET:
                handleGetStatistics(exchange);
                break;
            default:
                System.out.println("unknown method: " + exchange.getRequestMethod());
                sendNotAllowed(exchange);
        }
    }

    private void handleGetStatistics(HttpExchange exchange) throws IOException {
        QueryParser parser = new QueryParser(exchange.getRequestURI().getQuery());
        if (parser.getParam(USERNAME_PARAM_KEY).isEmpty()) {
            sendNotAllowed(exchange);
            return;
        }
        String username = parser.getParam(USERNAME_PARAM_KEY).getFirst();
        Optional<WordleServerStatistic> userStatisticOpt = getStatisticForUser(username);
        if (userStatisticOpt.isEmpty()) {
            sendNotFound(exchange);
            System.out.println("Statistics not found for: " + username);
            return;
        }
        sendText(exchange, gson.toJson(userStatisticOpt.get()));
        System.out.println("Get statistics handled");
    }

    private void handlePostStatistics(HttpExchange exchange) throws IOException {
        WordleClientResult result = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                new WordleServerClientResultTypeToken().getType()
        );
        sendText(exchange, gson.toJson(postStatistics(result)));
    }

    private WordleServerStatisticItem postStatistics(WordleClientResult result) throws IOException {
        WordleServerStatisticItem item = statisticsCache.incrementUserStatistic(result.username());
        loader.save(statisticsCache);
        return item;
    }

    private Optional<WordleServerStatistic> getStatisticForUser(String username) {
        return statisticsCache.getUserStatistic(username, USER_RATING_BEFORE_COUNT, USER_RATING_AFTER_COUNT);
    }
}