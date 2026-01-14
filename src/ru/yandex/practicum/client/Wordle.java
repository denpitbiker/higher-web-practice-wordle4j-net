package ru.yandex.practicum.client;

import ru.yandex.practicum.client.game.WordleDictionary;
import ru.yandex.practicum.client.game.WordleDictionaryLoader;
import ru.yandex.practicum.client.game.WordleGame;
import ru.yandex.practicum.client.network.WordleClient;
import ru.yandex.practicum.client.util.Logger;
import ru.yandex.practicum.common.dto.clientresult.WordleClientResult;
import ru.yandex.practicum.client.exception.WordleGameException;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatistic;
import ru.yandex.practicum.common.dto.serverstatistic.WordleServerStatisticItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ru.yandex.practicum.common.ServerConst.SERVER_ADDR;
import static ru.yandex.practicum.common.ServerConst.SERVER_PORT;

public class Wordle {
    private final String TAG = getClass().getSimpleName();
    private static final String LOGS_FILE = "log.txt";
    private static final String WORDS_FILE = "words_ru.txt";
    private static final String USERNAME_CHECK_REGEX = "[A-zА-я0-9 ]+";


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
                        new WordleDictionaryLoader(logger).loadWords(WORDS_FILE, WordleGame.WORD_LENGTH);
                WordleGame wordleGame = new WordleGame(logger, wordleDictionary);
                new Wordle(logger).playGame(wordleGame);
            } catch (Exception e) {
                e.printStackTrace(logger);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playGame(WordleGame wordleGame) throws IOException, InterruptedException {
        logger.log(TAG, "Начало игры");
        Scanner scanner = new Scanner(System.in);
        wordleGame.reset();
        String lastCandidate = "";
        System.out.println("Угадайте слово из пяти букв, у вас шесть попыток \nEnter - ввод слова или подсказка");
        while (!wordleGame.isEnd()) {
            logger.log(TAG, "Ждём ввода слова");
            String candidate = scanner.nextLine();
            String guess = wordleGame.guessWord(lastCandidate);
            if (candidate.isBlank()) {
                logger.log(TAG, "Пользователь воспользовался подсказкой: " + guess);
                candidate = guess;
                System.out.println(candidate);
            } else {
                logger.log(TAG, "Пользователь ввёл слово: " + candidate);
            }

            String resume;
            try {
                resume = wordleGame.checkWord(candidate);
                String state = wordleGame.getState();
                logger.log(TAG, "Результат проверки слова получен");
                System.out.println(resume + " " + state);
                lastCandidate = candidate;
            } catch (WordleGameException e) {
                System.out.println(e.getMessage());
                logger.log(TAG, "Ошибка при проверке слова");
                e.printStackTrace(logger);
            }
        }
        System.out.println("Правильный ответ: " + wordleGame.getAnswer());
        logger.log(TAG, "Игра окончена");
        if (lastCandidate.equalsIgnoreCase(wordleGame.getAnswer())) {
            System.out.println("Поздравляем с победой!");
            String username;
            do {
                System.out.println("Если хотите опубликовать результат, введите свой никнейм (допустимы буквы, цифры, пробел):");
                username = scanner.nextLine();
                if (username.isBlank()) {
                    return;
                }
            } while (!username.matches(USERNAME_CHECK_REGEX));
            sendResult(new WordleClientResult(username));
        }
        scanner.close();
    }

    private void sendResult(WordleClientResult result) throws IOException, InterruptedException {
        wordleClient.sendResult(result);
        WordleServerStatistic statistic = wordleClient.getStatistic(result.getUsername());
        System.out.println("  № | Никнейм                    | Статистика побед");
        System.out.println("____________________________________________________");
        for (int pos = 0; pos < statistic.getRating().size(); pos++) {
            WordleServerStatisticItem item = statistic.getRating().get(pos);
            System.out.printf("%3d | %-25s\t | %d\n", pos + statistic.getStartPosition() + 1, item.getUsername(), item.getCount());
        }
    }
}
