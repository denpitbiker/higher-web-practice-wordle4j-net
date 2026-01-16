package ru.yandex.practicum.client;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.client.exception.WordleGameWordNotFoundInDictionary;
import ru.yandex.practicum.client.game.WordleDictionary;
import ru.yandex.practicum.client.game.WordleDictionaryLoader;
import ru.yandex.practicum.client.game.WordleGame;
import ru.yandex.practicum.client.game.WordleGameState;
import ru.yandex.practicum.client.network.WordleClient;
import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.common.ServerConst;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticTypeToken;
import ru.yandex.practicum.server.WordleServer;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.TestsStubs.*;

public class WordleTest {
    private static final String ANSWER = "арбуз";
    private static final String USERNAME = "DEN";
    private static final int TWO_ATTEMPTS = 2;
    private static final String SCENARIO_SKIP_SEND_RESULT = """
            ТыКвА
            арбуз
            
            """;
    private static final String SCENARIO_SEND_RESULT = """
            арбуз
            DEN
            
            """;
    private static final String SCENARIO_SEND_RESULT_2 = """
            арбуз
            AB
            
            """;
    private static final String SCENARIO_USE_GUESS_RESULT = """
            
            арбуз
            
            """;
    private static final String TARGET_SCORE_OUTPUT = """
              № | Никнейм                    | Статистика побед
            ____________________________________________________
              1 | AB                       	 | 2
              2 | DEN                      	 | 1
            """;

    private static final String TEMP_FILE_SUFFIX = ".json";
    private final WordleServer wordleServer = new WordleServer();
    private File tmpStatsFile;
    private Wordle wordle;
    private static Logger logger;
    private WordleGame game;
    private static WordleDictionary dictionary;

    @BeforeAll
    public static void init() throws IOException {
        logger = new Logger(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        dictionary =
                new WordleDictionaryLoader(logger).loadWords(WORDS_FILE, WordleGame.WORD_LENGTH, VALID_CHARS_REGEX);
    }

    @BeforeEach
    public void setUp() throws IOException {
        // given
        tmpStatsFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), TEMP_FILE_SUFFIX);
        try (
                FileWriter fw = new FileWriter(tmpStatsFile.getAbsolutePath());
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(INITIAL_STATISTIC_JSON_CONTENT);
        }
        wordleServer.start(tmpStatsFile.getAbsolutePath());
        game = new WordleGame(logger, dictionary);
        wordle = new Wordle(logger);
    }

    @AfterEach
    public void shutDown() {
        wordleServer.stop();
        tmpStatsFile.delete();
        // Closing a ByteArrayInputStream has no effect. (see docs)
    }

    @Test
    @DisplayName("Проверка, что возможен запрос подсказки")
    public void playGame_CanUseHint_HintUsed() throws IOException, InterruptedException, WordleGameWordNotFoundInDictionary {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(SCENARIO_USE_GUESS_RESULT.getBytes());
        game.setAnswer(ANSWER);
        // when
        wordle.playGame(game, inputStream);
        // then
        assertTrue(game.hasUsedHint(), "Подсказка использовалась");
    }

    @Test
    @DisplayName("Проверка, что возможен ввод слова")
    public void playGame_RecognizesUserWordInput_WordRecognized() throws IOException, InterruptedException, WordleGameWordNotFoundInDictionary {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(SCENARIO_SKIP_SEND_RESULT.getBytes());
        game.setAnswer(ANSWER);
        // when
        wordle.playGame(game, inputStream);
        // then
        assertEquals(TWO_ATTEMPTS, game.getUsedAttempts(), "Было две попытки");
    }

    @Test
    @DisplayName("Проверка, что статистика отправляется")
    public void playGame_SendUserStatistic_StatisticSent() throws IOException, InterruptedException, WordleGameWordNotFoundInDictionary {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(SCENARIO_SEND_RESULT.getBytes());
        game.setAnswer(ANSWER);
        // when
        wordle.playGame(game, inputStream);
        // then
        assertEquals(WordleGameState.GameProgressState.WIN, game.getGameProgressState(), "Игра выиграна");
        WordleServerStatistic statisticFromFile;
        try (
                FileReader fr = new FileReader(tmpStatsFile);
                BufferedReader br = new BufferedReader(fr)
        ) {
            statisticFromFile = WordleServer.getGson().fromJson(br, new WordleServerStatisticTypeToken().getType());
        }
        assertDoesNotThrow(() -> statisticFromFile.getRating().getFirst(), "Данные не были отправлены на сервер");
        String usernameAtServer = statisticFromFile.getRating().getFirst().getUsername();
        assertEquals(USERNAME, usernameAtServer, "Данные не были отправлены на сервер");
    }

    @Test
    @DisplayName("Проверка, что статистика корректно отображается")
    public void createScoresString_GetUserStatistic_StatisticInValidFormat() throws IOException, InterruptedException, WordleGameWordNotFoundInDictionary {
        // given
        ByteArrayInputStream inputStream;
        for (int i = 0; i < TWO_ATTEMPTS; i++) {
            inputStream = new ByteArrayInputStream(SCENARIO_SEND_RESULT_2.getBytes());
            game.setAnswer(ANSWER);
            wordle.playGame(game, inputStream);
        }
        inputStream = new ByteArrayInputStream(SCENARIO_SEND_RESULT.getBytes());
        game.setAnswer(ANSWER);
        wordle.playGame(game, inputStream);
        WordleClient client = new WordleClient(logger, ServerConst.SERVER_ADDR, ServerConst.SERVER_PORT);
        // when
        String scoresString = wordle.createScoresString(client.getStatistic(USERNAME));
        // then
        assertEquals(TARGET_SCORE_OUTPUT, scoresString, "Результат игры выведен в некорректном формате");
    }
}
