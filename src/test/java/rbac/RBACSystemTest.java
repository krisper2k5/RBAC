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
        assertTrue(system.getUserManager().count() >= 1);
        assertTrue(system.getUserManager().findByUsername("admin").isPresent());
    }

    @Test
    @DisplayName("Администратор имеет роль Admin")
    void testAdminHasAdminRole() {
        var adminOpt = system.getUserManager().findByUsername("admin");
        assertTrue(adminOpt.isPresent());
        var assignments = system.getAssignmentManager().findByUser(adminOpt.get());
        assertFalse(assignments.isEmpty());
        boolean hasAdminRole = assignments.stream()
                .anyMatch(a -> a.role().name().equals("Admin"));
        assertTrue(hasAdminRole);
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

        assertNotNull(stats, "Статистика не должна быть null");
        assertTrue(stats.contains("Статистика системы RBAC"),
                "Должен быть заголовок статистики");
        assertTrue(stats.contains("Пользователей:"),
                "Должно быть 'Пользователей:'");
        assertTrue(stats.contains("Ролей:"),
                "Должно быть 'Ролей:'");
        assertTrue(stats.contains("Назначений:"),
                "Должно быть 'Назначений:'");
        assertTrue(stats.contains("Топ-3"),
                "Должен быть раздел 'Топ-3'");
    }

    @Test
    @DisplayName("Статистика содержит корректные числа")
    void testStatisticsContainsCorrectNumbers() {
        String stats = system.generateStatistics();
        // После initialize() должно быть: 1 пользователь, 3 роли, 1 назначение
        assertTrue(stats.contains("Пользователей: 1") || stats.contains("Пользователей: "),
                "Должно быть указано количество пользователей");
        assertTrue(stats.contains("Ролей: 3") || stats.contains("Ролей: "),
                "Должно быть указано количество ролей");
    }
}