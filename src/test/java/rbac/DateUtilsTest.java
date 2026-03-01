package rbac;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {
    @Test void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test void testIsBefore() {
        assertTrue(DateUtils.isBefore("2024-01-01", "2024-12-31"));
    }

    @Test void testAddDays() {
        String result = DateUtils.addDays("2024-01-15", 10);
        assertEquals("2024-01-25", result);
    }

    @Test void testFormatRelativeTime() {
        String today = DateUtils.getCurrentDate();
        assertEquals("сегодня", DateUtils.formatRelativeTime(today));
    }

    @Test void testIsExpired() {
        assertTrue(DateUtils.isExpired("2020-01-01"));
        assertFalse(DateUtils.isExpired("2099-12-31"));
    }
}