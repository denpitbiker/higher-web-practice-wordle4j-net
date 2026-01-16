package ru.yandex.practicum.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticTypeToken;
import ru.yandex.practicum.server.repository.WordleServerStatisticRepository;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class WordleServerStatisticRepositoryTest {
    private static final String TEMP_FILE_SUFFIX = ".json";
    private static final String UNKNOWN_FILE = "STUB.gfsgfsdgsfd";
    private static final int STUB_JSON_STATISTICS_RATING_SIZE = 3;
    private static final String STUB_JSON_STATISTICS_FIRST_USERNAME = "ПРИВЕТ";
    private static final int STUB_JSON_STATISTICS_LAST_USER_COUNT = 18;
    private static final int STUB_JSON_STATISTICS_START_POSITION = 0;
    private static final String STUB_JSON_STATISTICS_STRING = """
            {
              "startPosition": 0,
              "rating": [
                {
                  "username": "ПРИВЕТ",
                  "count": 21
                },
                {
                  "username": "ЧД?",
                  "count": 20
                },
                {
                  "username": "М)",
                  "count": 18
                }
                ]
            }""";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File tmpFile;
    private WordleServerStatisticRepository repository;

    @BeforeEach
    public void initRepository() throws IOException {
        tmpFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), TEMP_FILE_SUFFIX);
        try (
                FileWriter fw = new FileWriter(tmpFile.getAbsolutePath());
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(STUB_JSON_STATISTICS_STRING);
        }
        repository = new WordleServerStatisticRepository(gson, tmpFile.getAbsolutePath());
    }

    @AfterEach
    public void clean() {
        tmpFile.delete();
    }

    @Test
    @DisplayName("Проверка загрузки из несуществующего файла")
    public void load_LoadFromNonExistingFile_EmptyStatistics() throws IOException {
        // given
        repository = new WordleServerStatisticRepository(gson, UNKNOWN_FILE);
        // when
        WordleServerStatistic statistic = repository.load();
        // then
        assertTrue(statistic.getRating().isEmpty(), "Статистика должна быть пустой");
    }

    @Test
    @DisplayName("Проверка загрузки из существующего файла")
    public void load_LoadFromExistingFile_CorrectStatistics() throws IOException {
        // when
        WordleServerStatistic statistic = repository.load();
        // then
        assertEquals(STUB_JSON_STATISTICS_RATING_SIZE, statistic.getRating().size(), "Статистика должна быть длины 3");
        assertEquals(STUB_JSON_STATISTICS_START_POSITION, statistic.getStartPosition(), "Начальный индекс не совпал");
        assertEquals(STUB_JSON_STATISTICS_FIRST_USERNAME, statistic.getRating().getFirst().getUsername(), "Имя первого пользователя не совпало");
        assertEquals(STUB_JSON_STATISTICS_LAST_USER_COUNT, statistic.getRating().getLast().getCount(), "Счет последнего пользователя не совпал");
    }

    @Test
    @DisplayName("Проверка записи в файл")
    public void save_SaveToFile_SuccessSave() throws IOException {
        // given
        WordleServerStatistic statistic = gson.fromJson(STUB_JSON_STATISTICS_STRING, new WordleServerStatisticTypeToken().getType());
        statistic.incrementUserStatistic(statistic.getRating().getFirst().getUsername());
        // when
        repository.save(statistic);
        WordleServerStatistic statisticFromFile;
        try (
                FileReader fr = new FileReader(tmpFile.getAbsolutePath());
                BufferedReader br = new BufferedReader(fr)
        ) {
            statisticFromFile = gson.fromJson(br, new WordleServerStatisticTypeToken().getType());
        }
        // then
        assertEquals(statistic.getRating().getFirst().getCount(), statisticFromFile.getRating().getFirst().getCount(), "Статистика записана в файл неверно");
    }
}
