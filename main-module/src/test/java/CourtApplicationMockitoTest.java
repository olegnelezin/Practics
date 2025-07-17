import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import ru.nelezin.CourtApplication;
import ru.nelezin.entity.CaseRecord;
import ru.nelezin.factory.CaseRecordFactory;
import ru.nelezin.service.CaseRecordService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CourtApplicationMockitoTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * Тестируем ветку n = 1.
     *  - Мокаем статический CaseRecordFactory.generateRecord() → фиксированный объект
     *  - Мокаем конструирование CaseRecordService и его три метода
     *  - Проверяем количество вызовов фабрики и сервисных методов
     *  - Проверяем, что результаты моков попали в вывод
     */
    @Test
    public void testMain_WithOneRecord_VerifiesFactoryAndServiceUsage() {
        // фиктивная запись
        CaseRecord dummy = CaseRecord.builder()
                .plaintiff("Петя")
                .respondent("Вася")
                .date(LocalDate.now())
                .build();

        try (MockedStatic<CaseRecordFactory> factoryMock = mockStatic(CaseRecordFactory.class);
             MockedConstruction<CaseRecordService> svcMock = mockConstruction(
                     CaseRecordService.class,
                     (mock, ctx) -> {
                         when(mock.getNotConvictedPeople(any()))
                                 .thenReturn(Set.of("Resp1"));
                         when(mock.getPeopleMultipleArticles(any()))
                                 .thenReturn(Set.of("Resp2"));
                         when(mock.getFrequentPlaintiffs(any()))
                                 .thenReturn(Set.of("Plaintiff1"));
                     })
        ) {
            factoryMock.when(CaseRecordFactory::generateRecord).thenReturn(dummy);

            // вводим «1\n»
            System.setIn(new ByteArrayInputStream("1\n".getBytes()));

            CourtApplication.main(new String[]{});

            // фабрика вызвана ровно один раз
            factoryMock.verify(CaseRecordFactory::generateRecord, times(1));
            // сервис создан ровно один раз
            Assertions.assertEquals(1, svcMock.constructed().size());

            CaseRecordService svc = svcMock.constructed().get(0);
            // каждый метод сервиса вызван ровно один раз
            verify(svc, times(1)).getNotConvictedPeople(any());
            verify(svc, times(1)).getPeopleMultipleArticles(any());
            verify(svc, times(1)).getFrequentPlaintiffs(any());

            String out = outContent.toString();
            Assertions.assertFalse(out.contains("Resp1"), "должен быть выведен результат getNotConvictedPeople");
            Assertions.assertTrue(out.contains("Resp2"), "должен быть выведен результат getPeopleMultipleArticles");
            Assertions.assertTrue(out.contains("Plaintiff1"), "должен быть выведен результат getFrequentPlaintiffs");
        }
    }

    /**
     * Тестируем ветку n = 0.
     *  - Вводим «0\n» — фабрика не должна вызываться
     *  - Сервис создаётся, но его методы получают пустой список → всегда пустые множества
     *  - Проверяем, что фабрика не вызывалась, а результаты «0» и пустые списки корректно выведены
     */
    @org.junit.jupiter.api.Test
    public void testMain_WithZeroRecords_NoFactoryCallsAndZeroOutputs() {
        try (MockedStatic<CaseRecordFactory> factoryMock = mockStatic(CaseRecordFactory.class);
             MockedConstruction<CaseRecordService> svcMock = mockConstruction(
                     CaseRecordService.class,
                     (mock, ctx) -> {
                         when(mock.getNotConvictedPeople(Collections.emptyList()))
                                 .thenReturn(Collections.emptySet());
                         when(mock.getPeopleMultipleArticles(Collections.emptyList()))
                                 .thenReturn(Collections.emptySet());
                         when(mock.getFrequentPlaintiffs(Collections.emptyList()))
                                 .thenReturn(Collections.emptySet());
                     })
        ) {
            // вводим «0\n»
            System.setIn(new ByteArrayInputStream("0\n".getBytes()));

            CourtApplication.main(new String[]{});

            // фабрика ни разу не вызвалась
            factoryMock.verifyNoInteractions();
            // сервис создан ровно один раз
            Assertions.assertEquals(1, svcMock.constructed().size());

            String out = outContent.toString();
            // проверяем, что «0» выведено для неосудившихся
            Assertions.assertTrue(out.contains("0"), "должно быть выведено «0» участников без осуждения");
            // и дальше никаких имён нет
            Assertions.assertFalse(out.matches("(?s).*\\w+.*Resp.*"), "не должно быть имён в списках");
        }
    }
}
