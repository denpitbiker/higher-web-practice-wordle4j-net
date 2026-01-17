package ru.yandex.practicum.client.game;

import ru.yandex.practicum.client.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {
    private final String TAG = getClass().getSimpleName();
    private final Logger logger;

    public WordleDictionaryLoader(Logger logger) {
        this.logger = logger;
    }

    public WordleDictionary loadWords(String fileName, int wordLength, String validCharsRegex) throws IOException {
        if (wordLength < 1) throw new RuntimeException("Неверно задана длина слова для чтения из файла");
        WordleDictionary wordleDictionary = new WordleDictionary(wordLength, validCharsRegex);
        File dictionaryFile = getDictionaryFile(fileName);
        logger.log(TAG, "Открыт файл словаря: " + dictionaryFile.getAbsolutePath());
        wordleDictionary.addAll(loadWordsToList(dictionaryFile));
        logger.log(TAG, "Словарь для игры сформирован");
        return wordleDictionary;
    }

    private List<String> loadWordsToList(File wordsFile) throws IOException {
        logger.log(TAG, "Загружаем слова из файла");
        List<String> words = new ArrayList<>();
        try (
                FileReader fileReader = new FileReader(wordsFile, StandardCharsets.UTF_8);
                BufferedReader wordsReader = new BufferedReader(fileReader)
        ) {
            while (wordsReader.ready()) {
                words.add(wordsReader.readLine());
            }
            logger.log(TAG, "Прочитано слов из файла: " + words.size());
        } catch (IOException e) {
            logger.log(TAG, "Ошибка чтения файла");
            throw e;
        }
        return words;
    }

    private File getDictionaryFile(String dictionaryFile) throws FileNotFoundException {
        Path wordsFilePath = Paths.get("", dictionaryFile);
        logger.log(TAG, "Попытка открыть файл словаря: " + wordsFilePath.toAbsolutePath());
        File wordsFile = wordsFilePath.toFile();
        if (!wordsFile.exists()) {
            logger.log(TAG, "Файл \"" + dictionaryFile + "\" не был найден");
            throw new FileNotFoundException();
        }
        return wordsFile;
    }
}
