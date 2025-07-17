import org.junit.Test;
import ru.nelezin.entity.CaseRecord;
import ru.nelezin.enums.Article;
import ru.nelezin.factory.CaseRecordFactory;

import java.time.LocalDate;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseRecordFactoryTest {

    @Test
    public void testGenerateRecord_CreatesNonNull() {
        CaseRecord record = CaseRecordFactory.generateRecord();
        assertNotNull(record);
    }

    @Test
    public void testGenerateRecord_RespondentNotEqualToPlaintiff() {
        CaseRecord record = CaseRecordFactory.generateRecord();
        assertNotEquals(record.getRespondent(), record.getPlaintiff());
    }

    @Test
    public void testGenerateRecord_DateWithinRange() {
        LocalDate twoDecadesAgo = LocalDate.now().minusYears(20);
        CaseRecord record = CaseRecordFactory.generateRecord();

        assertTrue(record.getDate().isAfter(twoDecadesAgo) || record.getDate().isEqual(twoDecadesAgo));
        assertTrue(record.getDate().isBefore(LocalDate.now().plusDays(1))); // today or earlier
    }

    @Test
    public void testGenerateRecord_ArticleIsFromEnum() {
        boolean found = false;
        CaseRecord record = CaseRecordFactory.generateRecord();

        for (Article article : Article.values()) {
            if (article == record.getArticle()) {
                found = true;
                break;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testGenerateRecord_IsConvictedIsRandom() {
        boolean hasTrue = false;
        boolean hasFalse = false;

        for (int i = 0; i < 100; i++) {
            CaseRecord record = CaseRecordFactory.generateRecord();
            if (record.isConvicted()) {
                hasTrue = true;
            } else {
                hasFalse = true;
            }

            if (hasTrue && hasFalse) {
                break;
            }
        }

        assertTrue("Должны встречаться и true, и false", hasTrue && hasFalse);
    }
}
