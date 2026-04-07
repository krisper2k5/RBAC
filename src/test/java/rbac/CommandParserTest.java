package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты CommandParser")
class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        scanner = new Scanner(System.in);

        // Регистрируем тестовую команду
        parser.registerCommand("test-cmd", "Тестовая команда", (s, sys) -> {});
    }

    @Test
    @DisplayName("Регистрация команды")
    void testRegisterCommand() {
        assertDoesNotThrow(() ->
                parser.registerCommand("new-command", "Описание", (s, sys) -> {}));
    }

    @Test
    @DisplayName("Выполнение существующей команды")
    void testExecuteExistingCommand() {
        assertDoesNotThrow(() ->
                parser.executeCommand("test-cmd", scanner, system));
    }

    @Test
    @DisplayName("Обработка несуществующей команды")
    void testExecuteNonExistingCommand() {
        assertDoesNotThrow(() ->
                parser.executeCommand("nonexistent", scanner, system));
    }

    @Test
    @DisplayName("Вывод справки")
    void testPrintHelp() {
        assertDoesNotThrow(() -> parser.printHelp());
    }

    @Test
    @DisplayName("Парсинг и выполнение команды")
    void testParseAndExecute() {
        assertDoesNotThrow(() ->
                parser.parseAndExecute("test-cmd", scanner, system));
        assertDoesNotThrow(() ->
                parser.parseAndExecute("", scanner, system));
        assertDoesNotThrow(() ->
                parser.parseAndExecute(null, scanner, system));
    }

    @Test
    @DisplayName("Регистронезависимость команд")
    void testCaseInsensitivity() {
        parser.registerCommand("MyCommand", "Тест", (s, sys) -> {});
        assertDoesNotThrow(() ->
                parser.executeCommand("mycommand", scanner, system));
        assertDoesNotThrow(() ->
                parser.executeCommand("MYCOMMAND", scanner, system));
    }

    @Test
    @DisplayName("Команда с аргументами")
    void testCommandWithArguments() {
        final String[] capturedArgs = new String[1];
        parser.registerCommand("echo", "Эхо", (s, sys) -> {
            capturedArgs[0] = s.nextLine();
        });

        Scanner testScanner = new Scanner("test argument\n");
        parser.parseAndExecute("echo", testScanner, system);
        assertEquals("test argument", capturedArgs[0]);
    }
}