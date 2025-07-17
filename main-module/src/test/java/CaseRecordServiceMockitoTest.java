import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nelezin.entity.CaseRecord;
import ru.nelezin.enums.Article;
import ru.nelezin.service.CaseRecordService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaseRecordServiceMockitoTest {

    private CaseRecordService service;

    @BeforeEach
    void setup() {
        service = new CaseRecordService();
    }

    @Test
    void testGetNotConvictedPeople_filtersConvicted() {
        // Мокируем два кейс‑рекорда
        CaseRecord r1 = mock(CaseRecord.class);
        when(r1.isConvicted()).thenReturn(false);
        when(r1.getRespondent()).thenReturn("RespA");

        CaseRecord r2 = mock(CaseRecord.class);
        when(r2.isConvicted()).thenReturn(true);
        when(r2.getRespondent()).thenReturn("RespB");

        List<CaseRecord> input = List.of(r1, r2);

        Set<String> result = service.getNotConvictedPeople(input);

        // Ожидаем только одного "RespA"
        assertEquals(Set.of("RespA"), result);

        // Верифицируем, что у первого вызвалось isConvicted() и getRespondent(),
        // а у второго — только isConvicted(), map не пошёл
        verify(r1, times(1)).isConvicted();
        verify(r1, times(1)).getRespondent();

        verify(r2, times(1)).isConvicted();
        verify(r2, never()).getRespondent();
    }

    @Test
    void testGetPeopleMultipleArticles_identifiesMultipleArticles() {
        LocalDate now    = LocalDate.now();
        LocalDate recent = now.minusYears(5);

        // Две записи для одного респондента "X" с разными статьями
        CaseRecord r1 = mock(CaseRecord.class);
        when(r1.getDate()).thenReturn(recent);
        when(r1.getRespondent()).thenReturn("X");
        when(r1.getArticle()).thenReturn(Article.MURDER);

        CaseRecord r2 = mock(CaseRecord.class);
        when(r2.getDate()).thenReturn(recent);
        when(r2.getRespondent()).thenReturn("X");
        when(r2.getArticle()).thenReturn(Article.FRAUD);

        // Одна запись для "Y" — одна статья
        CaseRecord r3 = mock(CaseRecord.class);
        when(r3.getDate()).thenReturn(recent);
        when(r3.getRespondent()).thenReturn("Y");
        when(r3.getArticle()).thenReturn(Article.THEFT);

        // Старый кейс более 10 лет назад — его стрим отфильтрует по дате
        CaseRecord old = mock(CaseRecord.class);
        when(old.getDate()).thenReturn(now.minusYears(15));
        when(old.getRespondent()).thenReturn("X");
        when(old.getArticle()).thenReturn(Article.ROBBERY);

        List<CaseRecord> input = List.of(r1, r2, r3, old);

        Set<String> result = service.getPeopleMultipleArticles(input);

        // Только "X" должен попасть в результат
        assertEquals(Set.of("X"), result);

        // Проверим, что геттеры у одного из элементов точно вызвались
        verify(r1, times(1)).getDate();
        verify(r1, times(1)).getRespondent();
        verify(r1, times(1)).getArticle();
    }

    @Test
    void testGetFrequentPlaintiffs_identifiesFrequent() {
        LocalDate now    = LocalDate.now();
        LocalDate recent = now.minusYears(2);

        // Две записи с одним истцом "P1"
        CaseRecord r1 = mock(CaseRecord.class);
        when(r1.getDate()).thenReturn(recent);
        when(r1.getPlaintiff()).thenReturn("P1");

        CaseRecord r2 = mock(CaseRecord.class);
        when(r2.getDate()).thenReturn(recent);
        when(r2.getPlaintiff()).thenReturn("P1");

        // Одна запись с "P2"
        CaseRecord r3 = mock(CaseRecord.class);
        when(r3.getDate()).thenReturn(recent);
        when(r3.getPlaintiff()).thenReturn("P2");

        // Старая запись (более 3 лет назад) — не учитывается
        CaseRecord old = mock(CaseRecord.class);
        when(old.getDate()).thenReturn(now.minusYears(4));
        when(old.getPlaintiff()).thenReturn("P1");

        List<CaseRecord> input = List.of(r1, r2, r3, old);

        Set<String> result = service.getFrequentPlaintiffs(input);

        // Только "P1" — у него две свежие записи
        assertEquals(Set.of("P1"), result);

        verify(r1, times(1)).getDate();
        verify(r1, times(1)).getPlaintiff();
    }

    @Test
    void testEmptyInputs_returnEmptySets() {
        // На пустом списке все методы должны вернуть пустой Set
        assertTrue(service.getNotConvictedPeople(Collections.emptyList()).isEmpty());
        assertTrue(service.getPeopleMultipleArticles(Collections.emptyList()).isEmpty());
        assertTrue(service.getFrequentPlaintiffs(Collections.emptyList()).isEmpty());
    }
}
