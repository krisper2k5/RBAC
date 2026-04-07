package rbac;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private AuditLog log;

    @BeforeEach
    void setUp() {
        log = new AuditLog();
    }

    @Test
    void testLogEntry() {
        log.log("CREATE_USER", "admin", "john_doe", "Initial setup");
        assertEquals(1, log.size());
    }

    @Test
    void testGetByPerformer() {
        log.log("ACTION1", "admin", "target1", "details");
        log.log("ACTION2", "user", "target2", "details");
        List<AuditEntry> entries = log.getByPerformer("admin");
        assertEquals(1, entries.size());
    }

    @Test
    void testGetByAction() {
        log.log("DELETE", "admin", "user1", "reason");
        log.log("CREATE", "admin", "user2", "reason");
        List<AuditEntry> entries = log.getByAction("DELETE");
        assertEquals(1, entries.size());
    }
}