package com.taxi.notification.service;

import com.taxi.common.enums.NotificationStatus;
import com.taxi.notification.entity.NotificationTask;
import com.taxi.notification.repository.NotificationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTaskProcessor {

    private final NotificationTaskRepository taskRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.worker.pool-size:4}")
    private int poolSize;

    @Value("${app.worker.max-retries:3}")
    private int maxRetries;

    private ExecutorService workerPool;
    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        workerPool = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "notification-worker");
            t.setUncaughtExceptionHandler((thread, ex) ->
                    log.error("Worker {} crashed", thread.getName(), ex));
            return t;
        });

        for (int i = 0; i < poolSize; i++) {
            workerPool.submit(this::workerLoop);
        }
        log.info("Started {} notification workers", poolSize);
    }

    private void workerLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 1. Атомарно забираем задачу в транзакции
                NotificationTask task = transactionTemplate.execute(status ->
                        taskRepository.claimNextPendingTask().orElse(null));

                if (task == null) {
                    // Нет задач → ждём
                    Thread.sleep(500);
                    continue;
                }

                // 2. Обрабатываем задачу (вне транзакции захвата)
                processTask(task);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Worker cycle error", e);
            }
        }
        log.info("Worker {} stopped", Thread.currentThread().getName());
    }

    private void processTask(NotificationTask task) {
        // Помечаем как PROCESSING
        transactionTemplate.executeWithoutResult(status -> {
            task.setStatus(NotificationStatus.PROCESSING);
            task.setAttempts(task.getAttempts() + 1);
            taskRepository.save(task);
        });

        try {
            // Имитация отправки уведомления
            log.info("📤 Sending [{}] to {}: {}",
                    task.getRecipientType(), task.getRecipientId(), task.getMessage());
            Thread.sleep(100); // задержка

            // Успех
            transactionTemplate.executeWithoutResult(status -> {
                task.setStatus(NotificationStatus.SENT);
                task.setProcessedAt(java.time.LocalDateTime.now());
                taskRepository.save(task);
            });
            log.info("✅ Notification SENT for task {}", task.getId());

        } catch (Exception e) {
            log.warn("⚠️ Failed to send notification for task {}", task.getId(), e);

            // Логика повторов
            transactionTemplate.executeWithoutResult(status -> {
                if (task.getAttempts() >= maxRetries) {
                    task.setStatus(NotificationStatus.FAILED);
                    task.setProcessedAt(java.time.LocalDateTime.now());
                    log.error("❌ Task {} FAILED after {} attempts", task.getId(), maxRetries);
                } else {
                    // Возвращаем в очередь для повторной обработки
                    task.setStatus(NotificationStatus.PENDING);
                    log.info("🔄 Task {} returned to queue (attempt {}/{})",
                            task.getId(), task.getAttempts(), maxRetries);
                }
                taskRepository.save(task);
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Stopping workers gracefully...");
        running = false;
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Workers didn't stop in time, forcing shutdown");
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("✅ All workers stopped");
    }
}