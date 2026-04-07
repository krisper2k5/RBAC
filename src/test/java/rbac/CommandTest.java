package rbac;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты Command интерфейса")
class CommandTest {

    @Test
    @DisplayName("Command - функциональный интерфейс")
    void testCommandIsFunctionalInterface() {
        // Проверяем что интерфейс можно использовать как лямбду
        Command command = (scanner, system) -> {};
        assertNotNull(command);
    }

    @Test
    @DisplayName("Command выполняется без ошибок")
    void testCommandExecution() {
        RBACSystem system = new RBACSystem();
        system.initialize();
        Scanner scanner = new Scanner(System.in);

        Command command = (s, sys) -> {
            sys.setCurrentUser("test");
        };

        assertDoesNotThrow(() -> command.execute(scanner, system));
        assertEquals("test", system.getCurrentUser());
    }
}