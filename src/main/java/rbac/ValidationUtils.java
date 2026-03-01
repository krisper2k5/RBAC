package rbac;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2})?$");

    // Проверка имени пользователя: 3-20 символов, латиница, цифры, подчёркивание
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    // Проверка email: наличие @ и домена
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // Проверка формата даты: YYYY-MM-DD или YYYY-MM-DD HH:MM
    public static boolean isValidDate(String date) {
        return date != null && DATE_PATTERN.matcher(date.trim()).matches();
    }

    // Нормализация строки: trim, удаление лишних пробелов, приведение регистра
    public static String normalizeString(String input, boolean toLower) {
        if (input == null) return null;
        String normalized = input.trim().replaceAll("\\s+", " ");
        return toLower ? normalized.toLowerCase() : normalized;
    }

    // Проверка на пустую строку с выбросом исключения
        public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }

    // Проверка на null с выбросом исключения
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
}