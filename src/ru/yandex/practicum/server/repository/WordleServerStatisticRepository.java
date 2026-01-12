package ru.yandex.practicum.server.repository;

import com.google.gson.Gson;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticTypeToken;

import java.io.*;
import java.util.ArrayList;

public class WordleServerStatisticRepository {
    private final String jsonDatabaseFile;
    private final Gson gson;

    public WordleServerStatisticRepository(Gson gson, String jsonDatabaseFile) {
        this.jsonDatabaseFile = jsonDatabaseFile;
        this.gson = gson;
    }

    public WordleServerStatistic load() throws IOException {
        if (!new File(jsonDatabaseFile).exists()) {
            return new WordleServerStatistic(new ArrayList<>());
        }
        try (
                FileReader fr = new FileReader(jsonDatabaseFile);
                BufferedReader br = new BufferedReader(fr)
        ) {
            return gson.fromJson(br, new WordleServerStatisticTypeToken().getType());
        }
    }

    public void save(WordleServerStatistic statistic) throws IOException {
        try (
                FileWriter fw = new FileWriter(jsonDatabaseFile);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            gson.toJson(statistic, bw);
        }
    }
}
