package example;

public class CalculationTask implements Runnable {
    private final int threadOrderNumber;
    private final int calculationLength;
    private final int stepDelayMs;

    public CalculationTask(int threadOrderNumber, int calculationLength, int stepDelayMs) {
        this.threadOrderNumber = threadOrderNumber;
        this.calculationLength = calculationLength;
        this.stepDelayMs = stepDelayMs;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();

        try {
            for (int i = 0; i <= calculationLength; i++) {
                Thread.sleep(stepDelayMs);

                int percent = (int) ((i / (float) calculationLength) * 100);
                String progressBar = buildProgressBar(i, calculationLength);

                ConsoleWriter.printProgress(threadOrderNumber, threadId, percent, progressBar);
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            ConsoleWriter.printFinish(threadOrderNumber, threadId, totalTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String buildProgressBar(int current, int total) {
        int width = 20;
        int filled = (int) ((current / (float) total) * width);
        int empty = width - filled;

        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < filled; k++) sb.append("-");
        if (current < total) sb.append(">");
        for (int k = 0; k < empty - (current < total ? 1 : 0); k++) sb.append(" ");

        return sb.toString();
    }
}