package rbac;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        runTests();

        runInteractiveConsole();
    }

    private static void runTests() {
        System.out.println("=== Тест 1.1: Валидация пользователя ===");
        try {
            User u1 = User.validate("john_doe", "Иван Иванов", "ivan@example.com");
            System.out.println("✓ Валидный пользователь: " + u1.format());
        } catch (Exception e) {
            System.out.println("✗ Ошибка: " + e.getMessage());
        }

        try {
            User u2 = User.validate("ab", "Короткое", "неправильный-email");
        } catch (Exception e) {
            System.out.println("✓ Поймана ошибка валидации: " + e.getMessage());
        }

        System.out.println("\n=== Тест 1.2: Права доступа ===");
        Permission p1 = new Permission("read", "USERS", "Просмотр списка пользователей");
        Permission p2 = new Permission("WRITE", "reports", "Редактирование отчётов");
        System.out.println("✓ " + p1.format());
        System.out.println("✓ " + p2.format());
        System.out.println("✓ Поиск по шаблону 'READ': " + p1.matches("READ", null));

        System.out.println("\n=== Тест 1.3: Роль ===");
        Role viewer = new Role("Наблюдатель", "Только для чтения");
        viewer.addPermission(p1);
        viewer.addPermission(new Permission("VIEW", "reports", "Просмотр отчётов"));
        System.out.println(viewer.format());

        System.out.println("\n=== Тест 1.4: Метаданные ===");
        AssignmentMetadata meta = AssignmentMetadata.now("администратор", "Первоначальная настройка");
        System.out.println(meta.format());

        System.out.println("\n=== Тест 1.7: Постоянное назначение ===");
        User admin = User.validate("admin", "Администратор Системы", "admin@company.com");
        PermanentAssignment pa = new PermanentAssignment(admin, viewer, meta);
        System.out.println(pa.summary());

        System.out.println("\n=== Тест 1.8: Временное назначение ===");
        AssignmentMetadata tempMeta = AssignmentMetadata.now("менеджер", "Временный доступ для аудита");
        TemporaryAssignment ta = new TemporaryAssignment(
                admin,
                viewer,
                tempMeta,
                "2026-12-31 23:59"
        );
        System.out.println(ta.summary());

        System.out.println("\n=== Тесты завершены ===\n");
    }

    private static void runInteractiveConsole() {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║         RBAC System - Управление доступом                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Система инициализирована. Текущий пользователь: " + system.getCurrentUser());
        System.out.println("Введите 'help' для списка команд.\n");

        while (true) {
            System.out.print("rbac> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                parser.executeCommand("exit", scanner, system);
                break;
            }

            parser.parseAndExecute(input, scanner, system);
        }

        scanner.close();
    }
}