package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {
    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
    }

    @Test
    void testAddAndGetRole() {
        Role role = new Role("Admin", "Administrator role");
        roleManager.add(role);

        Optional<Role> found = roleManager.findById(role.id());
        assertTrue(found.isPresent());
        assertEquals("Admin", found.get().name());
    }

    @Test
    void testFindByName() {
        Role role = new Role("Editor", "Editor role");
        roleManager.add(role);

        Optional<Role> found = roleManager.findByName("Editor");
        assertTrue(found.isPresent());
        assertEquals("Editor role", found.get().description());
    }

    @Test
    void testAddPermissionToRole() {
        Role role = new Role("Viewer", "Viewer role");
        roleManager.add(role);

        Permission permission = new Permission("READ", "USERS", "Read users");
        roleManager.addPermissionToRole("Viewer", permission);

        Optional<Role> found = roleManager.findByName("Viewer");
        assertTrue(found.isPresent());
        assertTrue(found.get().hasPermission(permission));
    }

    @Test
    void testRemovePermissionFromRole() {
        Role role = new Role("Editor", "Editor role");
        Permission permission = new Permission("WRITE", "REPORTS", "Write reports");
        role.addPermission(permission);
        roleManager.add(role);

        roleManager.removePermissionFromRole("Editor", permission);

        Optional<Role> found = roleManager.findByName("Editor");
        assertTrue(found.isPresent());
        assertFalse(found.get().hasPermission(permission));
    }

    @Test
    void testFilterByPermission() {
        Role role1 = new Role("Viewer", "Viewer role");
        role1.addPermission(new Permission("READ", "USERS", "Read users"));
        roleManager.add(role1);

        Role role2 = new Role("Editor", "Editor role");
        role2.addPermission(new Permission("WRITE", "REPORTS", "Write reports"));
        roleManager.add(role2);

        List<Role> filtered = roleManager.findRolesWithPermission("READ", "USERS");
        assertEquals(1, filtered.size());
        assertEquals("Viewer", filtered.get(0).name());
    }

    @Test
    void testSortByPermissionCount() {
        Role role1 = new Role("Small", "Small role");
        Role role2 = new Role("Big", "Big role");
        role2.addPermission(new Permission("READ", "USERS", "Read users"));
        role2.addPermission(new Permission("WRITE", "REPORTS", "Write reports"));
        roleManager.add(role1);
        roleManager.add(role2);

        List<Role> sorted = roleManager.findAll(null, RoleSorters.byPermissionCount());
        assertEquals("Small", sorted.get(0).name());
        assertEquals("Big", sorted.get(1).name());
    }
}