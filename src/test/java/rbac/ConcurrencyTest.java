package rbac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты потокобезопасности менеджеров")
class ConcurrencyTest {

    @Test
    @DisplayName("Параллельное создание пользователей, ролей и назначений")
    void testConcurrentOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        int threadsCount = 10;
        int opsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadsCount);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        Runnable worker = () -> {
            try {
                startLatch.await();
                long threadId = Thread.currentThread().getId();

                for (int i = 0; i < opsPerThread; i++) {
                    String unique = String.format("t%d_%02d", threadId, i);
                    String username = "user_" + unique; //
                    String email = unique + "@test.com";
                    String roleName = "role_" + unique;

                    try {
                        system.getUserManager().add(new User(username, "Full " + unique, email));
                    } catch (IllegalArgumentException ignored) {}

                    try {
                        system.getRoleManager().add(new Role(roleName, "Desc " + unique));
                    } catch (IllegalArgumentException ignored) {}

                    system.getUserManager().findByFilter(UserFilters.byUsernameContains("user_"));
                    system.getRoleManager().findAll(null, RoleSorters.byName());
                    system.getAssignmentManager().getActiveAssignments();

                    system.getAuditLog().log("CONC_TEST", "system", "target_" + unique, "Concurrent op");
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < threadsCount; i++) {
            executor.submit(worker);
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Обнаружены исключения в потоках: " + errors);

        assertTrue(system.getUserManager().count() > 0, "Пользователи должны быть созданы");
        assertTrue(system.getRoleManager().count() > 0, "Роли должны быть созданы");
        assertTrue(system.getAuditLog().size() >= threadsCount * opsPerThread, "Лог должен содержать все записи");
    }
}