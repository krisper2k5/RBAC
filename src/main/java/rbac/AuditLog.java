package rbac;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());

    // очередь для асинхронного логирования
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>(10000);
    private final Thread workerThread;
    private volatile boolean running = true;

    public AuditLog() {
        workerThread = new Thread(this::processQueue, "AuditLog-Worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void log(String action, String performer, String target, String details) {
        entries.add(AuditEntry.create(action, performer, target, details));
    }

    // асинхронное логирование через очередь
    public void logAsync(String action, String performer, String target, String details) {
        try {
            queue.offer(AuditEntry.create(action, performer, target, details), 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processQueue() {
        while (running || !queue.isEmpty()) {
            try {
                AuditEntry entry = queue.poll(100, TimeUnit.MILLISECONDS);
                if (entry != null) entries.add(entry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        try { workerThread.join(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public List<AuditEntry> getAll() { return new ArrayList<>(entries); }
    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream().filter(e -> e.performer().equalsIgnoreCase(performer)).collect(Collectors.toList());
    }
    public List<AuditEntry> getByAction(String action) {
        return entries.stream().filter(e -> e.action().equalsIgnoreCase(action)).collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) { System.out.println("Лог аудита пуст."); return; }
        System.out.println("\n=== Аудит-лог (" + entries.size() + " записей) ===\n");
        for (AuditEntry entry : entries) System.out.println(entry);
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) writer.println(entry);
            System.out.println("Лог сохранён в " + filename);
        } catch (IOException e) { System.err.println("Ошибка сохранения лога: " + e.getMessage()); }
    }

    public void clear() { entries.clear(); }
    public int size() { return entries.size(); }
}