package rbac;
import java.util.concurrent.*;

public class BackgroundExecutor {
    private final ExecutorService executor;
    private static volatile BackgroundExecutor instance;

    private BackgroundExecutor() {
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "RBAC-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public static BackgroundExecutor getInstance() {
        if (instance == null) {
            synchronized (BackgroundExecutor.class) {
                if (instance == null) instance = new BackgroundExecutor();
            }
        }
        return instance;
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}