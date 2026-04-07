package rbac;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandParser {
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final Map<String, String> commandDescriptions = new ConcurrentHashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());
        if (command == null) {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка доступных команд.");
            return;
        }
        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    public void printHelp() {
        System.out.println("\n=== Доступные команды ===\n");
        List<String> sortedCommands = new ArrayList<>(commands.keySet());
        Collections.sort(sortedCommands);

        for (String cmd : sortedCommands) {
            System.out.printf("  %-25s - %s\n", cmd, commandDescriptions.get(cmd));
        }
        System.out.println();
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) return;
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        executeCommand(commandName, scanner, system);
    }
}