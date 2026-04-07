package rbac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Нагрузочный тест фоновых задач и многопоточности")
class WorkersLoadTest {

    @Test
    @DisplayName("Параллельное создание/обновление пользователей и ролей без сбоев")
    void testConcurrentWorkersAndOps() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int threads = 10;
        int ops = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        Runnable worker = () -> {
            try {
                start.await();
                String id = String.valueOf(Thread.currentThread().getId());
                for (int i = 0; i < ops; i++) {
                    String uName = "worker_u_" + id + "_" + i;
                    String rName = "worker_r_" + id + "_" + i;

                    try {
                        User u = new User(uName, "Worker " + id, "w" + id + i + "@test.com");
                        system.getUserManager().add(u);
                    } catch (IllegalArgumentException ignored) {}

                    try {
                        Role r = new Role(rName, "Role for " + id);
                        system.getRoleManager().add(r);
                    } catch (IllegalArgumentException ignored) {}

                    system.getUserManager().findByFilter(UserFilters.byUsernameContains("worker_u_"));
                    system.getRoleManager().findAll(null, RoleSorters.byName());
                    system.getAssignmentManager().getActiveAssignments();

                    system.getAuditLog().logAsync("LOAD_TEST", "worker_" + id, "target_" + i, "Concurrent op");
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < threads; i++) executor.submit(worker);
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "Тест не должен зависать");
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Обнаружены исключения: " + errors);
        assertTrue(system.getUserManager().count() > 1, "Пользователи должны быть созданы");
        assertTrue(system.getRoleManager().count() > 3, "Роли должны быть созданы");
    }

    @Test
    @DisplayName("Асинхронные методы execute без блокировок")
    void testAsyncMethodsExecuteCorrectly() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        long start = System.currentTimeMillis();
        system.generateUserReportAsync("test_report.txt");
        system.saveDataAsync("test_backup.txt");
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 500, "Асинхронные методы должны возвращать управление мгновенно. Прошло: " + duration + "мс");
    }
}