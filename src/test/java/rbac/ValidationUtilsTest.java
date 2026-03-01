package rbac;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {
    @Test void testValidUsername() { assertTrue(ValidationUtils.isValidUsername("user_123")); }
    @Test void testInvalidUsername() { assertFalse(ValidationUtils.isValidUsername("ab")); }
    @Test void testValidEmail() { assertTrue(ValidationUtils.isValidEmail("test@example.com")); }
    @Test void testInvalidEmail() { assertFalse(ValidationUtils.isValidEmail("invalid")); }
    @Test void testValidDate() { assertTrue(ValidationUtils.isValidDate("2024-01-15")); }
    @Test void testNormalizeString() { assertEquals("hello world", ValidationUtils.normalizeString("  HELLO  WORLD  ", true)); }
    @Test void testRequireNonEmpty() { assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("", "Field")); }
}