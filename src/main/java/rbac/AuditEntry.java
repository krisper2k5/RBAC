package rbac;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AuditEntry create(String action, String performer, String target, String details) {
        return new AuditEntry(
                LocalDateTime.now().format(FORMATTER),
                action,
                performer,
                target,
                details != null ? details : "-"
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s → %s | %s",
                timestamp, action, performer, target,
                details != null ? details : "-");
    }
}