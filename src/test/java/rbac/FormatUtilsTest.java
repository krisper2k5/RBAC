package rbac;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {
    @Test void testTruncate() {
        assertEquals("Hello...", FormatUtils.truncate("Hello World", 8));
        assertEquals("Hi", FormatUtils.truncate("Hi", 10));
    }

    @Test void testPadRight() {
        assertEquals("test    ", FormatUtils.padRight("test", 8));
    }

    @Test void testPadLeft() {
        assertEquals("    test", FormatUtils.padLeft("test", 8));
    }

    @Test void testFormatTable() {
        String[] headers = {"Name", "Age"};
        String[][] rows = {{"Alice", "30"}, {"Bob", "25"}};
        String table = FormatUtils.formatTable(headers, java.util.List.of(rows));
        assertTrue(table.contains("Alice"));
        assertTrue(table.contains("┌"));
    }
}