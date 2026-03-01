package rbac;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Текущая дата в формате YYYY-MM-DD
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    // Текущая дата и время в формате YYYY-MM-DD HH:MM:SS
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    // Сравнение: date1 < date2
    public static boolean isBefore(String date1, String date2) {
        return parseDate(date1).isBefore(parseDate(date2));
    }

    // Сравнение: date1 > date2
    public static boolean isAfter(String date1, String date2) {
        return parseDate(date1).isAfter(parseDate(date2));
    }

    // Добавить дни к дате
    public static String addDays(String date, int days) {
        LocalDate parsed = parseDate(date);
        return parsed.plusDays(days).format(DATE_FORMATTER);
    }

    // Форматирование относительного времени
    public static String formatRelativeTime(String date) {
        LocalDate parsed = parseDate(date);
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(parsed, today);

        if (days == 0) {
            return "сегодня";
        } else if (days == 1) {
            return "вчера";
        } else if (days == -1) {
            return "завтра";
        } else if (days > 0) {
            return days + " дн. назад";
        } else {
            return "через " + (-days) + " дн.";
        }
    }

    // Парсинг даты
    public static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        String trimmed = dateTime.trim();
        if (trimmed.length() == 10) {
            return LocalDate.parse(trimmed, DATE_FORMATTER).atTime(23, 59, 59);
        }
        return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // Парсинг только даты
    public static LocalDate parseDate(String date) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        return LocalDate.parse(date.trim(), DATE_FORMATTER);
    }

    // Форматирование даты из LocalDateTime
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    // Проверка, истекла ли дата
    public static boolean isExpired(String expirationDate) {
        if (expirationDate == null) return false;
        return LocalDateTime.now().isAfter(parseDateTime(expirationDate));
    }

    // Получение оставшихся дней до даты
    public static long getDaysUntil(String targetDate) {
        LocalDate target = parseDate(targetDate);
        return ChronoUnit.DAYS.between(LocalDate.now(), target);
    }
}