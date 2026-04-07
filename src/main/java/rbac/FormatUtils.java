package rbac;
import java.util.*;

public class FormatUtils {

    // Форматирование данных в ASCII-таблицу

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        // Вычисляем ширину колонок
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, widths.length); i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("┌").append(join("┬", widths, "─")).append("┐\n");

        sb.append("│").append(formatRow(headers, widths)).append("│\n");
        sb.append("├").append(join("┼", widths, "─")).append("┤\n");

        for (String[] row : rows) {
            sb.append("│").append(formatRow(row, widths)).append("│\n");
        }

        sb.append("└").append(join("┴", widths, "─")).append("┘");

        return sb.toString();
    }

    private static String formatRow(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.length) ? cells[i] : "";
            sb.append(" ").append(padRight(cell, widths[i])).append(" │");
        }
        return sb.toString();
    }

    private static String join(String separator, int[] widths, String fillChar) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) sb.append(separator);
            sb.append(String.valueOf(fillChar).repeat(widths[i]));
        }
        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxWidth = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);

        StringBuilder sb = new StringBuilder();
        sb.append("┌").append("─".repeat(maxWidth + 2)).append("┐\n");
        for (String line : lines) {
            sb.append("│ ").append(padRight(line, maxWidth)).append(" │\n");
        }
        sb.append("└").append("─".repeat(maxWidth + 2)).append("┘");
        return sb.toString();
    }

    public static String formatHeader(String text) {
        return "\n" + "═".repeat(40) + "\n  " + text + "\n" + "═".repeat(40) + "\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return " ".repeat(length - text.length()) + text;
    }

    public static String center(String text, int width) {
        if (text == null || text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}