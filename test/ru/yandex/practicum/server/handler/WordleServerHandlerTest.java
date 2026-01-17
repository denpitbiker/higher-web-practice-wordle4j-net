package ru.yandex.practicum.server.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.client.network.ApiUrlBuilder;
import ru.yandex.practicum.common.ServerConst;
import ru.yandex.practicum.server.WordleServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static ru.yandex.practicum.TestsStubs.INITIAL_STATISTIC_JSON_CONTENT;

public abstract class WordleServerHandlerTest {
    private static final String TEMP_FILE_SUFFIX = ".json";
    protected ApiUrlBuilder apiUrlBuilder = new ApiUrlBuilder(ServerConst.SERVER_ADDR, String.valueOf(ServerConst.SERVER_PORT));
    protected WordleServer wordleServer = new WordleServer();
    protected Gson gson = WordleServer.getGson();
    protected File tmpStatsFile;

    @BeforeEach
    public void setUp() throws IOException {
        tmpStatsFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), TEMP_FILE_SUFFIX);
        try (
                FileWriter fw = new FileWriter(tmpStatsFile.getAbsolutePath());
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(INITIAL_STATISTIC_JSON_CONTENT);
        }
        wordleServer.start(tmpStatsFile.getAbsolutePath());
    }

    @AfterEach
    public void shutDown() {
        wordleServer.stop();
        tmpStatsFile.delete();
    }
}
