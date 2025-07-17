import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nelezin.CourtApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourtApplicationTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testApplication_RunWithInput_OutputContainsResults() {
        // Подделываем ввод: пользователь вводит 100 записей
        String simulatedInput = "100\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Запускаем приложение
        CourtApplication.main(new String[]{});

        // Проверяем, что вывод содержит нужные фразы
        String output = outContent.toString();

        assertTrue(output.contains("Count the number of people who participated in the trials but were not convicted:"));
        assertTrue(output.contains("Find people who participated in the processes in more than 1 article over the past 10 years:"));
        assertTrue(output.contains("Find people who have sued more than 1 time in the last 3 years:"));
    }
}