package ru.yandex.practicum.client;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.client.game.WordleDictionary;
import ru.yandex.practicum.client.game.WordleDictionaryLoader;
import ru.yandex.practicum.client.util.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.TestsStubs.*;

public class WordleDictionaryLoaderTest {
    private WordleDictionaryLoader loader;

    @BeforeEach
    public void init() {
        Logger logger = new Logger(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        loader = new WordleDictionaryLoader(logger);
    }

    @Test
    @DisplayName("Проверка загрузки слов из существующего файла")
    public void loadWords_LoadWordsFromExistingFile_WordsList() throws IOException {
        // when
        WordleDictionary dictionary = loader.loadWords(WORDS_FILE, WORDS_LENGTH, VALID_CHARS_REGEX);
        // then
        assertFalse(dictionary.isEmpty(), "Файл не был прочитан");
    }

    @Test
    @DisplayName("Проверка загрузки слов из несуществующего файла")
    public void loadWords_LoadWordsFromNonExistingFile_FileNotFoundException() throws IOException {
        // given
        String stubFileName = String.valueOf(System.currentTimeMillis());
        // then
        assertThrows(FileNotFoundException.class, () -> loader.loadWords(stubFileName, WORDS_LENGTH, VALID_CHARS_REGEX), "Должно быть выброшено исключение FileNotFoundException");
    }
}
