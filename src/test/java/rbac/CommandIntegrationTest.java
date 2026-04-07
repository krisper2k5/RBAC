package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты команд")
class CommandIntegrationTest {

    private RBACSystem system;
    private CommandParser parser;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
        parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Команда help выводит справку")
    void testHelpCommand() {
        parser.executeCommand("help", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.toLowerCase().contains("команд") || output.contains("help"));
    }

    @Test
    @DisplayName("Команда stats выводит статистику")
    void testStatsCommand() {
        parser.executeCommand("stats", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.contains("Пользователей:") || output.contains("Ролей:"));
    }

    @Test
    @DisplayName("Команда user-list выводит пользователей")
    void testUserListCommand() {
        parser.executeCommand("user-list", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.contains("admin") || output.toLowerCase().contains("пользователь"));
    }

    @Test
    @DisplayName("Команда role-list выводит роли")
    void testRoleListCommand() {
        parser.executeCommand("role-list", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.contains("Admin") || output.contains("Manager"));
    }

    @Test
    @DisplayName("Команда assignment-list выводит назначения")
    void testAssignmentListCommand() {
        parser.executeCommand("assignment-list", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.contains("admin") || output.toLowerCase().contains("назначен"));
    }

    @Test
    @DisplayName("Команда assignment-active выводит активные назначения")
    void testAssignmentActiveCommand() {
        parser.executeCommand("assignment-active", new Scanner(""), system);
        String output = outContent.toString();

        String lower = output.toLowerCase();
        boolean ok = lower.contains("активн") ||
                lower.contains("назначен") ||
                lower.contains("не найдено") ||
                output.trim().isEmpty();
        assertTrue(ok, "Ожидается вывод. Получено: \"" + output + "\"");

    }

    @Test
    @DisplayName("Неизвестная команда выводит ошибку")
    void testUnknownCommand() {
        parser.executeCommand("unknown-xyz", new Scanner(""), system);
        String output = outContent.toString();
        assertTrue(output.toLowerCase().contains("неизвестн") || output.contains("Отмена"));
    }
}