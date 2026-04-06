package rbac;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты планировщика задач (ScheduledExecutorService)")
class SchedulerTest {
    private RBACSystem system;
    private TaskScheduler scheduler;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
        scheduler = new TaskScheduler(system);
    }

    @AfterEach
    void tearDown() {
        scheduler.stop();
    }

    @Test
    @DisplayName("Очистка истёкших временных назначений")
    void testCleanupExpiredAssignments() throws InterruptedException {
        User u = new User("temp_user_1", "Temp User", "temp1@test.com");
        system.getUserManager().add(u);
        Role r = new Role("TempRole", "Временная роль");
        system.getRoleManager().add(r);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Тест");
        TemporaryAssignment expired = new TemporaryAssignment(u, r, meta, "2020-01-01 00:00");
        system.getAssignmentManager().add(expired);

        int countBefore = system.getAssignmentManager().count();
        scheduler.start(1);

        Thread.sleep(1200);

        assertEquals(countBefore - 1, system.getAssignmentManager().count(),
                "Истёкшее назначение должно быть удалено");
        assertTrue(system.getAuditLog().getAll().stream()
                        .anyMatch(e -> e.action().equals("CLEANUP_EXPIRED")),
                "Должна быть запись об очистке");
    }

    @Test
    @DisplayName("Периодическое логирование статистики")
    void testPeriodicStatsLogging() throws InterruptedException {
        scheduler.start(1);
        Thread.sleep(1500);

        boolean hasStats = system.getAuditLog().getAll().stream()
                .anyMatch(e -> e.action().contains("STATS"));
        assertTrue(hasStats, "Планировщик должен периодически логировать статистику");
    }

    @Test
    @DisplayName("Корректная остановка планировщика")
    void testGracefulShutdown() {
        scheduler.start(10);
        assertTrue(scheduler.isRunning());
        scheduler.stop();
        assertFalse(scheduler.isRunning());
        assertDoesNotThrow(() -> scheduler.stop());
    }
}