package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {
    private AssignmentManager assignmentManager;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        assignmentManager = new AssignmentManager();
        user = new User("test_user", "Test User", "test@example.com");
        role = new Role("TestRole", "Test role description");
    }

    @Test
    void testAddAndGetAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test reason");
        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
        assertTrue(found.isPresent());
        assertNotNull(found.orElseThrow(() -> new AssertionError("Назначение не найдено")));
    }

    @Test
    void testFindByUser() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test reason");
        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        List<RoleAssignment> assignments = assignmentManager.findByUser(user);
        assertEquals(1, assignments.size());
        assertEquals("TestRole", assignments.get(0).role().name());
    }

    @Test
    void testRevokeAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test reason");
        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        assignmentManager.add(assignment);

        assignmentManager.revokeAssignment(assignment.assignmentId());

        Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
        assertTrue(found.isPresent());
        assertFalse(found.orElseThrow(() -> new AssertionError("Назначение не найдено")).isActive());
    }

    @Test
    void testExtendTemporaryAssignment() {
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test reason");
        TemporaryAssignment assignment = new TemporaryAssignment(
                user, role, metadata, "2026-01-01 23:59"
        );
        assignmentManager.add(assignment);

        assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), "2027-01-01 23:59");

        Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
        assertTrue(found.isPresent());
        TemporaryAssignment updatedAssignment = (TemporaryAssignment) found.orElseThrow(() -> new AssertionError("Назначение не найдено"));
        assertEquals("2027-01-01 23:59", updatedAssignment.expiresAt());
    }
}