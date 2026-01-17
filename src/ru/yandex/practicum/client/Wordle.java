package ru.yandex.practicum.client;

import ru.yandex.practicum.client.exception.WordleGameNoAttemptsLeftException;
import ru.yandex.practicum.client.game.WordleDictionary;
import ru.yandex.practicum.client.game.WordleDictionaryLoader;
import ru.yandex.practicum.client.game.WordleGame;
import ru.yandex.practicum.client.game.WordleGameState;
import ru.yandex.practicum.client.network.WordleClient;
import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.common.dto.clientresult.WordleClientResult;
import ru.yandex.practicum.client.exception.WordleGameException;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ru.yandex.practicum.client.game.WordleGame.MAX_STEPS;
import static ru.yandex.practicum.common.ServerConst.SERVER_ADDR;
import static ru.yandex.practicum.common.ServerConst.SERVER_PORT;

public class Wordle {
    private final String TAG = getClass().getSimpleName();
    private static final String LOGS_FILE = "log.txt";
    private static final String WORDS_FILE = "words_ru.txt";
    private static final String USERNAME_CHECK_REGEX = "[A-zА-ЯЁа-яё0-9 ]+";
    private static final String VALID_CHARS_REGEX = "[А-ЯЁа-яё]+";

    private final Logger logger;
    private final WordleClient wordleClient;

    Wordle(Logger logger) {
        this.logger = logger;
        this.wordleClient = new WordleClient(logger, SERVER_ADDR, SERVER_PORT);
    }

    public static void main(String[] args) {
        try (
                FileOutputStream fos = new FileOutputStream(LOGS_FILE);
                Writer writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                Logger logger = new Logger(writer)
        ) {
            try {
                WordleDictionary wordleDictionary =
                        new WordleDictionaryLoader(logger).loadWords(WORDS_FILE, WordleGame.WORD_LENGTH, VALID_CHARS_REGEX);
                WordleGame wordleGame = new WordleGame(logger, wordleDictionary);
                new Wordle(logger).playGame(wordleGame, System.in);
            } catch (Exception e) {
                e.printStackTrace(logger);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playGame(WordleGame wordleGame, InputStream inputStream) throws IOException, InterruptedException {
        logger.log(TAG, "Начало игры");
        try (
                Scanner scanner = new Scanner(inputStream)
        ) {
            System.out.println("Угадайте слово из пяти букв, у вас шесть попыток \nEnter - ввод слова или подсказка");
            while (!wordleGame.isEnd()) {
                logger.log(TAG, "Ждём ввода слова");
                String candidate = scanner.nextLine();
                if (candidate.isBlank()) {
                    try {
                        candidate = wordleGame.guessWord();
                    } catch (WordleGameNoAttemptsLeftException e) {
                        throw new RuntimeException("Подсказка запросилась после завершения игры (кончились попытки)", e);
                    }
                    logger.log(TAG, "Пользователь воспользовался подсказкой: " + candidate);
                    System.out.println(candidate);
                } else {
                    logger.log(TAG, "Пользователь ввёл слово: " + candidate);
                }

                try {
                    String checkResult = wordleGame.checkWord(candidate) + ' ' + getAttemptsLeftText(wordleGame.getUsedAttempts());
                    logger.log(TAG, "Результат проверки слова получен: " + checkResult);
                    System.out.println(checkResult);
                } catch (WordleGameException e) {
                    System.out.println(e.getMessage());
                    logger.log(TAG, "Ошибка при проверке слова");
                    e.printStackTrace(logger);
                }
            }
            System.out.println("Правильный ответ: " + wordleGame.getAnswer());
            String gameResultLog = "Игра окончена: " + wordleGame.getGameProgressState() + ", попыток: " +
                    wordleGame.getUsedAttempts() + ", пользовался подсказкой: " + wordleGame.hasUsedHint(); // there's no real impact using StringBuilder
            logger.log(TAG, gameResultLog);
            if (wordleGame.getGameProgressState() == WordleGameState.GameProgressState.WIN) {
                System.out.println("Поздравляем с победой!");
                String username;
                do {
                    System.out.println("Если хотите опубликовать результат, введите свой никнейм (допустимы буквы, цифры, пробел):");
                    logger.log(TAG, "Запрос никнейма для статистики");
                    username = scanner.nextLine();
                    if (username.isBlank()) {
                        logger.log(TAG, "Пользователь отказался от отправки результата");
                        return;
                    }
                    logger.log(TAG, "Пользователь ввел никнейм: " + username);
                } while (!username.matches(USERNAME_CHECK_REGEX));
                sendResult(new WordleClientResult(username, wordleGame.getUsedAttempts(), wordleGame.hasUsedHint()));
                System.out.println(createScoresString(wordleClient.getStatistic(username)));
                logger.log(TAG, "Показана таблица результатов");
            }
        }
    }

    private String getAttemptsLeftText(int usedAttempts) {
        return String.format("попыток %d/%d", MAX_STEPS - usedAttempts, MAX_STEPS);
    }

    public String createScoresString(WordleServerStatistic statistic) {
        StringBuilder scores = new StringBuilder();
        scores.append("  № | Никнейм                    | Статистика побед\n");
        scores.append("____________________________________________________\n");
        for (int pos = 0; pos < statistic.getRating().size(); pos++) {
            WordleServerStatisticItem item = statistic.getRating().get(pos);
            scores.append(String.format("%3d | %-25s\t | %d\n", pos + statistic.getStartPosition() + 1, item.getUsername(), item.getCount()));
        }
        return scores.toString();
    }

    private void sendResult(WordleClientResult result) throws IOException, InterruptedException {
        logger.log(TAG, "Отправка статистики на сервер");
        wordleClient.sendResult(result);
    }
}
