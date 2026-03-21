package example;

public class Main {
    private static final int THREAD_COUNT = 7;
    private static final int CALCULATION_LENGTH = 100;
    private static final int STEP_DELAY_MS = 100;

    public static void main(String[] args) {
        System.out.println("\nЗапуск имитации многопоточного расчёта...\n");
        System.out.println("Конфигурация: Потоков=" + THREAD_COUNT +
                ", Шагов=" + CALCULATION_LENGTH +
                ", Задержка=" + STEP_DELAY_MS + "мс\n");

        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            CalculationTask task = new CalculationTask(i + 1, CALCULATION_LENGTH, STEP_DELAY_MS);

            threads[i] = new Thread(task);
            threads[i].start();

            try { Thread.sleep( 100); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}