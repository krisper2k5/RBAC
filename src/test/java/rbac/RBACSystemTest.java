package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты RBACSystem")
class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("Инициализация создаёт менеджеры")
    void testInitializationCreatesManagers() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
    }

    @Test
    @DisplayName("Инициализация создаёт предустановленные роли")
    void testInitializationCreatesRoles() {
        assertTrue(system.getRoleManager().count() >= 3,
                "Должно быть минимум 3 роли (Admin, Manager, Viewer)");

        assertTrue(system.getRoleManager().findByName("Admin").isPresent());
        assertTrue(system.getRoleManager().findByName("Manager").isPresent());
        assertTrue(system.getRoleManager().findByName("Viewer").isPresent());
    }

    @Test
    @DisplayName("Инициализация создаёт администратора")
    void testInitializationCreatesAdmin() {
        assertTrue(system.getUserManager().count() >= 1,
                "Должен быть минимум 1 пользователь");
        assertTrue(system.getUserManager().findByUsername("admin").isPresent());
    }

    @Test
    @DisplayName("Администратор имеет роль Admin")
    void testAdminHasAdminRole() {
        var adminOpt = system.getUserManager().findByUsername("admin");
        assertTrue(adminOpt.isPresent());

        var assignments = system.getAssignmentManager()
                .findByUser(adminOpt.get());
        assertFalse(assignments.isEmpty(),
                "У администратора должна быть хотя бы одна роль");

        boolean hasAdminRole = assignments.stream()
                .anyMatch(a -> a.role().name().equals("Admin"));
        assertTrue(hasAdminRole, "У администратора должна быть роль Admin");
    }

    @Test
    @DisplayName("getCurrentUser возвращает текущего пользователя")
    void testGetCurrentUser() {
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    @DisplayName("setCurrentUser меняет текущего пользователя")
    void testSetCurrentUser() {
        system.setCurrentUser("test_user");
        assertEquals("test_user", system.getCurrentUser());
    }

    @Test
    @DisplayName("generateStatistics возвращает статистику")
    void testGenerateStatistics() {
        String stats = system.generateStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Статистика системы RBAC"));
        assertTrue(stats.contains("Пользователей:"));
        assertTrue(stats.contains("Ролей:"));
        assertTrue(stats.contains("Назначений:"));
        assertTrue(stats.contains("Топ-3 самых популярных ролей"));
    }

    @Test
    @DisplayName("Статистика содержит корректные числа")
    void testStatisticsContainsCorrectNumbers() {
        String stats = system.generateStatistics();

        assertTrue(stats.contains("Пользователей: 1"));
        assertTrue(stats.contains("Ролей: 3"));
    }
}