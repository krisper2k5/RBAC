package example;

public class ConsoleWriter {

    private static final Object lock = new Object();

    public static void printProgress(int threadOrder, long threadId, int percent, String progressBar) {
        synchronized (lock) {
            String format = "Поток #%2d | ID: %3d | [%s] %3d%%";
            String line = String.format(format, threadOrder, threadId, progressBar, percent);

            System.out.print(line);
            System.out.print("\033[K");
            System.out.println();
            System.out.print("\033[A");
            System.out.flush();
        }
    }

    public static void printFinish(int threadOrder, long threadId, long totalTime) {
        synchronized (lock) {
            String format = "Поток #%2d | ID: %3d | [ГОТОВО] Время: %d мс";
            String line = String.format(format, threadOrder, threadId, totalTime);

            System.out.println(line);
            System.out.println();
            System.out.flush();
        }
    }
}