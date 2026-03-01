// AuditLog.java
package rbac;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();

    // Добавить запись в лог
    public void log(String action, String performer, String target, String details) {
        entries.add(AuditEntry.create(action, performer, target, details));
    }

    // Получить все записи
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    // Фильтр по исполнителю
    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    // Фильтр по действию
    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    // Форматированный вывод всех записей
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Лог аудита пуст.");
            return;
        }
        System.out.println("\n=== Аудит-лог (" + entries.size() + " записей) ===\n");
        for (AuditEntry entry : entries) {
            System.out.println(entry);
        }
    }

    // Сохранение лога в файл
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.println(entry);
            }
            System.out.println("✓ Лог сохранён в " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения лога: " + e.getMessage());
        }
    }

    // Очистка лога
    public void clear() {
        entries.clear();
    }

    // Количество записей
    public int size() {
        return entries.size();
    }
}