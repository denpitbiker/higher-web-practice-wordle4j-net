package ru.yandex.practicum.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.client.network.WordleClient;
import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.common.ServerConst;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.server.WordleServer;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.TestsStubs.*;

public class WordleClientTest {
   private static final String TEMP_FILE_SUFFIX = ".json";
    private final WordleServer wordleServer = new WordleServer();
    private File tmpStatsFile;
    private WordleClient client;

    @BeforeEach
    public void setUp() throws IOException {
        Logger logger = new Logger(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        client = new WordleClient(logger, ServerConst.SERVER_ADDR, ServerConst.SERVER_PORT);
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

    @Test
    @DisplayName("Отправка статистики игры пользователя")
    public void sendResult_SendUserResult_Success() {
        // then
        assertDoesNotThrow(() -> client.sendResult(USER_RESULT_1), "Отправка должна завершаться без ошибок");
    }

    @Test
    @DisplayName("Получение статистики по известному пользователю")
    public void getStatistic_GetStatisticKnownUser_Success() throws IOException, InterruptedException {
        // given
        client.sendResult(USER_RESULT_1);
        // when
        assertDoesNotThrow(() -> client.getStatistic(USERNAME_1), "Получение должно завершаться без ошибок");
        WordleServerStatistic statistic = client.getStatistic(USERNAME_1);
        // then
        assertEquals(USERNAME_1, statistic.getRating().getFirst().getUsername(), "Имя пользователя не совпадает");
    }

    @Test
    @DisplayName("Получение статистики по неизвестному пользователю")
    public void getStatistic_GetStatisticUnknownUser_RuntimeExceptionCode404() {
        // then
        assertThrows(RuntimeException.class,() -> client.getStatistic(USERNAME_1), "Получение должно завершаться c ошибкой");
    }
}
