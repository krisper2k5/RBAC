package rbac;
import java.util.concurrent.*;

public class TaskScheduler {
    private final ScheduledExecutorService scheduler;
    private final RBACSystem system;
    private volatile boolean running = false;

    public TaskScheduler(RBACSystem system) {
        this.system = system;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "RBAC-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(long intervalSeconds) {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(this::runCleanupTask, 0, intervalSeconds, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::runStatsLogTask, intervalSeconds / 2, intervalSeconds, TimeUnit.SECONDS);

        system.getAuditLog().log("SCHEDULER_START", "system", "TaskScheduler",
                "Запущен планировщик задач (интервал: " + intervalSeconds + "с)");
    }

    private void runCleanupTask() {
        try {
            int cleaned = system.getAssignmentManager().cleanupExpiredTemporaryAssignments();
            if (cleaned > 0) {
                system.getAuditLog().logAsync("CLEANUP_EXPIRED", "system", "assignments",
                        "Автоматически удалено истёкших назначений: " + cleaned);
            }
        } catch (Exception e) {
            System.err.println("Ошибка задачи очистки: " + e.getMessage());
        }
    }

    private void runStatsLogTask() {
        try {
            system.getAuditLog().logAsync("STATS_REPORT", "system", "RBAC", "Периодический отчёт статистики");
        } catch (Exception e) {
            System.err.println("Ошибка задачи логирования: " + e.getMessage());
        }
    }

    public void stop() {
        if (!running) return;
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        system.getAuditLog().log("SCHEDULER_STOP", "system", "TaskScheduler", "Остановлен планировщик");
    }

    public boolean isRunning() {
        return running;
    }
}