package ru.yandex.practicum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.server.handler.statistic.WordleServerStatisticHandler;

import java.io.*;
import java.net.InetSocketAddress;

import static ru.yandex.practicum.common.ServerConst.SERVER_PORT;

public class WordleServer {
    private static final int SERVER_BACKLOG = 0;
    private static final int SERVER_STOP_DELAY_SECONDS = 1;
    private static final String STATISTIC_DB_JSON = "stats.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private HttpServer server;

    public static void main(String[] args) throws IOException {
        new WordleServer().start(STATISTIC_DB_JSON);
    }

    public void start(String statisticDbFile) throws IOException {
        server = HttpServer.create(new InetSocketAddress(SERVER_PORT), SERVER_BACKLOG);
        server.createContext(WordleServerStatisticHandler.PATH, new WordleServerStatisticHandler(gson, statisticDbFile));
        server.start();
        System.out.println("Wordle Server Started");
    }

    public void stop() {
        server.stop(SERVER_STOP_DELAY_SECONDS);
        System.out.println("Wordle Server Stopped");
    }

    public static Gson getGson() {
        return gson;
    }
}
