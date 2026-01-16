package ru.yandex.practicum.client;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.client.exception.*;
import ru.yandex.practicum.client.game.WordleDictionary;
import ru.yandex.practicum.client.game.WordleDictionaryLoader;
import ru.yandex.practicum.client.game.WordleGame;
import ru.yandex.practicum.client.game.WordleGameState;
import ru.yandex.practicum.client.util.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.TestsStubs.VALID_CHARS_REGEX;
import static ru.yandex.practicum.TestsStubs.WORDS_FILE;

public class WordleGameTest {
    private static Logger logger;
    private static final int TEST_TIMEOUT_S = 5;
    private static WordleGame game;
    private static WordleDictionary wordleDictionary;
    private static final String VALID_WORD_1 = "арбуз";
    private static final String VALID_WORD_2 = "аргал";
    private static final String VALID_WORD_3 = "автор";
    private static final String VALID_WORD_4 = "КлАсС";
    private static final String UNKNOWN_VALID_WORD = "АбобА";
    private static final Set<Character> VALID_WORD_3_TO_1_CORRECT_CHARS = new HashSet<>(Arrays.asList('а', 'р'));
    private static final Set<Character> VALID_WORD_3_TO_1_WRONG_CHARS = new HashSet<>(Arrays.asList('в', 'т', 'о'));
    private static final String VALID_WORD_3_TO_1_PATTERN = "+---^";
    private static final String TOO_LONG_WORD = "прокрастинатор";
    private static final String TOO_SHORT_WORD = "м";
    private static final String INVALID_WORD = "аб!г9";
    private static final String INVALID_5_SPACES_WORD = "     ";
    private static final int ZERO_ATTEMPTS = 0;
    private static final int ONE_ATTEMPT = 1;
    private static final int TWO_ATTEMPTS = 2;
    private static final int THREE_ATTEMPTS = 3;
    private static final int FIVE_ATTEMPTS = 5;

    @BeforeAll
    public static void init() throws IOException {
        logger = new Logger(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        wordleDictionary =
                new WordleDictionaryLoader(logger).loadWords(WORDS_FILE, WordleGame.WORD_LENGTH, VALID_CHARS_REGEX);
    }

    @BeforeEach
    public void resetGame() throws WordleGameEmptyWordsCollectionException {
        // given
        game = new WordleGame(logger, wordleDictionary);
        game.reset();
    }

    @Test
    @DisplayName("Проверка, что getAnswer возвращает не null ответ игры")
    public void getAnswer_GetAnswerNotNull_StringAnswer() {
        // then
        assertNotNull(game.getAnswer(), "Ответ не должен быть null");
    }

    @Test
    @DisplayName("Проверка, что getAnswer возвращает валидный ответ игры")
    public void getAnswer_GetAnswerMatchesRequirements_ValidStringAnswer() {
        // when
        String answer = game.getAnswer();
        // then
        assertTrue(wordleDictionary.isCorrectWord(answer), "Ответ в некорректном формате");
    }

    @Test
    @Timeout(TEST_TIMEOUT_S)
    @DisplayName("Проверка, что setAnswer устанавливает новый ответ на игру")
    public void setAnswer_UpdatesAnswer_NewAnswer() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary {
        // given
        String oldAnswer;
        do {
            oldAnswer = game.getAnswer();
        } while (oldAnswer.equals(VALID_WORD_1));
        // when
        game.setAnswer(VALID_WORD_1);
        // then
        assertNotEquals(oldAnswer, game.getAnswer(), "Ответ должен был измениться");
    }

    @Test
    @DisplayName("Проверка, что setAnswer проверяет слово на наличие в коллекции")
    public void setAnswer_ChecksWordInDictionary_WordleGameWordNotFoundInDictionary() {
        // then
        assertThrows(WordleGameWordNotFoundInDictionary.class, () -> game.setAnswer(TOO_LONG_WORD), "Ожидалось исключение WordleGameWordNotFoundInDictionary");
        assertThrows(WordleGameWordNotFoundInDictionary.class, () -> game.setAnswer(INVALID_WORD), "Ожидалось исключение WordleGameWordNotFoundInDictionary");
    }

    @Test
    @DisplayName("Проверка, что hasUsedHint верно определяет использование подсказки")
    public void hasUsedHint_RecognizesHintUsage_HintUsed() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        assertFalse(game.hasUsedHint(), "Подсказка еще не использовалась");
        // when
        game.checkWord(VALID_WORD_2);
        assertFalse(game.hasUsedHint(), "Подсказка еще не использовалась");
        game.guessWord();
        // then
        assertTrue(game.hasUsedHint(), "Подсказка использовалась");
    }

    @Test
    @DisplayName("Проверка, что getUsedAttempts верно определяет количество использованных попыток")
    public void getUsedAttempts_CorrectAttemptsCounting() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        assertEquals(ZERO_ATTEMPTS, game.getUsedAttempts(), "Пользователь еще не угадывал слово");
        // when
        game.checkWord(VALID_WORD_2);
        assertEquals(ONE_ATTEMPT, game.getUsedAttempts(), "Пользователь угадывал слово один раз");
        String guess = game.guessWord();
        game.checkWord(guess);
        if (guess.equals(VALID_WORD_1)) {
            assertEquals(TWO_ATTEMPTS, game.getUsedAttempts(), "Пользователь угадывал слово два раза");
            return;
        }
        guess = game.guessWord();
        game.checkWord(guess);
        if (guess.equals(VALID_WORD_1)) {
            assertEquals(THREE_ATTEMPTS, game.getUsedAttempts(), "Пользователь угадывал слово три раза");
            return;
        }
        game.checkWord(VALID_WORD_2);
        game.checkWord(VALID_WORD_1);
        // then
        assertEquals(FIVE_ATTEMPTS, game.getUsedAttempts(), "Пользователь угадывал слово пять раз");
    }

    @Test
    @DisplayName("Проверка, что isEnd верно определяет окончание игры")
    public void isEnd_CorrectGameEndRecognition() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        assertFalse(game.isEnd(), "Пользователь еще не закончил игру");
        // when
        game.checkWord(VALID_WORD_2);
        assertFalse(game.isEnd(), "Пользователь еще не закончил игру");
        String guess = game.guessWord();
        if (guess.equals(VALID_WORD_1)) {
            assertTrue(game.isEnd(), "Пользователь уже угадал слово");
            return;
        }
        assertFalse(game.isEnd(), "Пользователь еще не закончил игру");
        game.checkWord(VALID_WORD_1);
        // then
        assertTrue(game.isEnd(), "Пользователь уже угадал слово");
    }

    @Test
    @DisplayName("Проверка, что getGameProgressState верно определяет окончание игры из-за закончившихся попыток")
    public void getGameProgressState_CheckProgressStateNoAttemptsLeft_NO_ATTEMPTS() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        assertEquals(WordleGameState.GameProgressState.PLAY, game.getGameProgressState(), "Пользователь еще не закончил игру");
        // when
        game.checkWord(VALID_WORD_2);
        assertEquals(WordleGameState.GameProgressState.PLAY, game.getGameProgressState(), "Пользователь еще не закончил игру");
        game.checkWord(VALID_WORD_2);
        game.checkWord(VALID_WORD_2);
        game.checkWord(VALID_WORD_2);
        game.checkWord(VALID_WORD_2);
        game.checkWord(VALID_WORD_2);
        // then
        assertEquals(WordleGameState.GameProgressState.NO_ATTEMPTS, game.getGameProgressState(), "У пользователя закончились попытки");
    }

    @Test
    @DisplayName("Проверка, что getGameProgressState верно определяет окончание игры из-за победы")
    public void getGameProgressState_CheckProgressStateWin_WIN() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        assertEquals(WordleGameState.GameProgressState.PLAY, game.getGameProgressState(), "Пользователь еще не закончил игру");
        // when
        game.checkWord(VALID_WORD_2);
        assertEquals(WordleGameState.GameProgressState.PLAY, game.getGameProgressState(), "Пользователь еще не закончил игру");
        game.checkWord(VALID_WORD_1);
        // then
        assertEquals(WordleGameState.GameProgressState.WIN, game.getGameProgressState(), "У пользователя закончились попытки");
    }

    @Test
    @DisplayName("Проверка, что reset перезапускает игру")
    public void reset_ResetsGame() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        game.checkWord(VALID_WORD_2);
        String guess = game.guessWord();
        if (!guess.equals(VALID_WORD_1)) {
            game.checkWord(VALID_WORD_1);
        }
        // when
        game.reset();
        // then
        assertNotEquals(VALID_WORD_1, game.getAnswer(), "Ответ должен был измениться");
        assertEquals(ZERO_ATTEMPTS, game.getUsedAttempts(), "Счетчик попыток должен обнуляться");
        assertEquals(WordleGameState.GameProgressState.PLAY, game.getGameProgressState(), "Игра должна была перейти в состояние PLAY");
        assertFalse(game.hasUsedHint(), "Игра должна была обнулить флаг использования подсказок");
    }

    @Test
    @DisplayName("Проверка, что guessWord выдает валидное слово из словаря, с учетом текущих знаний")
    public void guessWord_CheckGuessCorrectWithKnowledge_AllKnownWordsUsed() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        game.checkWord(VALID_WORD_3);
        // when
        String guess = game.guessWord();
        // then
        assertTrue(wordleDictionary.isCorrectWord(guess), "Сгенерировано невалидное слово");
        Set<Character> guessToValid1Intersection = new HashSet<>();
        boolean usedWrongChars = false;
        for (int i = 0; i < guess.length(); i++) {
            char ch = guess.charAt(i);
            if (VALID_WORD_3_TO_1_CORRECT_CHARS.contains(ch)) {
                guessToValid1Intersection.add(ch);
            }
            if (VALID_WORD_3_TO_1_WRONG_CHARS.contains(ch)) {
                usedWrongChars = true;
                break;
            }
        }
        assertFalse(usedWrongChars, "В подсказке используются символы, которые уже известны, как неправильные");
        int oldSize = guessToValid1Intersection.size();
        guessToValid1Intersection.addAll(VALID_WORD_3_TO_1_CORRECT_CHARS);
        assertEquals(oldSize, guessToValid1Intersection.size(), "Не все символы из опыта были учтены при угадывании");
    }

    @Test
    @DisplayName("Проверка, что checkWord валидирует ввод (кидает ошибки)")
    public void checkWord_ThrowsExceptionOnInvalidInput_WordleException() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary {
        // given
        game.setAnswer(VALID_WORD_1);
       // then
        assertThrows(WordleGameWordNotFoundInDictionary.class, () -> game.checkWord(UNKNOWN_VALID_WORD), "Слова нет в словаре");
        assertThrows(WordleGameIncorrectWordException.class, () -> game.checkWord(INVALID_WORD), "Слово некорректное (запретные символы)");
        assertThrows(WordleGameIncorrectWordException.class, () -> game.checkWord(INVALID_5_SPACES_WORD), "Слово некорректное");
        assertThrows(WordleGameIncorrectWordException.class, () -> game.checkWord(null), "Слово некорректное");
        assertThrows(WordleGameWrongWordLengthException.class, () -> game.checkWord(TOO_LONG_WORD), "Слово слишком длинное");
        assertThrows(WordleGameWrongWordLengthException.class, () -> game.checkWord(TOO_SHORT_WORD), "Слово слишком короткое");
        assertDoesNotThrow(() -> game.checkWord(VALID_WORD_4), "Слово корректное, ошибок быть не должно");
    }

    @Test
    @DisplayName("Проверка, что checkWord проверяет, остались ли еще попытки")
    public void checkWord_ThrowsExceptionOnNoAttempts_WordleGameNoAttemptsLeftException() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        // then
        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            game.checkWord(VALID_WORD_2);
        }
        assertThrows(WordleGameNoAttemptsLeftException.class, () -> game.checkWord(VALID_WORD_2), "Попытки закончились");
        game.setAnswer(VALID_WORD_1);
        assertDoesNotThrow(() -> game.checkWord(VALID_WORD_2), "Попытки еще есть");
    }

    @Test
    @DisplayName("Проверка, что checkWord отдает корректный паттерн совпадений для слова")
    public void checkWord_CheckWordReturnsCorrectWordPattern_CorrectPattern() throws WordleGameEmptyWordsCollectionException, WordleGameWordNotFoundInDictionary,
            WordleGameWrongWordLengthException, WordleGameIncorrectWordException, WordleGameNoAttemptsLeftException {
        // given
        game.setAnswer(VALID_WORD_1);
        // then
        assertEquals(VALID_WORD_3_TO_1_PATTERN, game.checkWord(VALID_WORD_3), "Неверный паттерн угадываемого слова");
    }
}
