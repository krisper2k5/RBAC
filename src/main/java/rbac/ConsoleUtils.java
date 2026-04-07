package rbac;
import java.util.*;
import java.util.function.Predicate;

public class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static String promptString(String message, boolean required) {
        return promptString(message, required, null);
    }

    public static String promptString(String message, boolean required, Predicate<String> validator) {
        while (true) {
            System.out.print(message + (required ? " *" : "") + ": ");
            String input = SCANNER.nextLine().trim();

            if (input.isEmpty() && !required) {
                return null;
            }
            if (input.isEmpty() && required) {
                System.out.println("Это поле обязательно для заполнения");
                continue;
            }
            if (validator != null && !validator.test(input)) {
                System.out.println("Неверный формат");
                continue;
            }
            return input;
        }
    }

    public static int promptInt(String message, int min, int max) {
        while (true) {
            System.out.print(message + " [" + min + "-" + max + "]: ");
            try {
                int value = Integer.parseInt(SCANNER.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Число должно быть в диапазоне [" + min + ", " + max + "]");
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число");
            }
        }
    }

    public static boolean promptYesNo(String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = SCANNER.nextLine().trim().toLowerCase();
            if (input.equals("да") || input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("нет") || input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("Введите 'да' или 'нет'");
        }
    }

    public static <T> T promptChoice(String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список опций не может быть пустым");
        }

        System.out.println("\n" + message);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, options.get(i).toString());
        }

        int choice = promptInt("Выберите номер", 1, options.size());
        return options.get(choice - 1);
    }

    public static String promptWithDefault(String message, String defaultValue) {
        String result = promptString(message + " [" + defaultValue + "]", false);
        return (result == null || result.isEmpty()) ? defaultValue : result;
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        int width = Math.max(title.length() + 4, 40);
        System.out.println("\n" + "═".repeat(width));
        System.out.println("  " + title);
        System.out.println("═".repeat(width) + "\n");
    }

    public static void printSuccess(String message) {
        System.out.println("Success" + message);
    }

    public static void printError(String message) {
        System.out.println("Error " + message);
    }

    public static void printWarning(String message) {
        System.out.println("Warning" + message);
    }
}