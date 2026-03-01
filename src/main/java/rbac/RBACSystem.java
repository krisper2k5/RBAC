package rbac;
import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "Просмотр списка пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");

        Permission readReports = new Permission("READ", "reports", "Просмотр отчётов");
        Permission writeReports = new Permission("WRITE", "reports", "Редактирование отчётов");

        Permission readSettings = new Permission("READ", "settings", "Просмотр настроек");
        Permission writeSettings = new Permission("WRITE", "settings", "Изменение настроек");

        Role admin = new Role("Admin", "Полный доступ ко всем функциям системы");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);
        admin.addPermission(readReports);
        admin.addPermission(writeReports);
        admin.addPermission(readSettings);
        admin.addPermission(writeSettings);

        Role manager = new Role("Manager", "Управление отчётами и пользователями");
        manager.addPermission(readUsers);
        manager.addPermission(writeUsers);
        manager.addPermission(readReports);
        manager.addPermission(writeReports);

        Role viewer = new Role("Viewer", "Только для чтения");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);
        viewer.addPermission(readSettings);

        roleManager.add(admin);
        roleManager.add(manager);
        roleManager.add(viewer);

        User adminUser = User.validate("admin", "Администратор Системы", "admin@company.com");
        userManager.add(adminUser);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Инициализация системы");
        PermanentAssignment assignment = new PermanentAssignment(adminUser, admin, meta);
        assignmentManager.add(assignment);

        setCurrentUser("admin");
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Статистика системы RBAC ===\n\n");

        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        sb.append("Пользователей: ").append(userCount).append("\n");
        sb.append("Ролей: ").append(roleCount).append("\n");
        sb.append("Назначений: ").append(assignmentCount)
                .append(" (активных: ").append(activeAssignments)
                .append(", истёкших: ").append(expiredAssignments).append(")\n");

        double avgRolesPerUser = userCount > 0 ?
                (double) assignmentCount / userCount : 0;
        sb.append(String.format("Среднее количество ролей на пользователя: %.2f\n", avgRolesPerUser));

        sb.append("\nТоп-3 самых популярных ролей:\n");
        Map<String, Long> rolePopularity = assignmentManager.findAll().stream()
                .collect(Collectors.groupingBy(a -> a.role().name(), Collectors.counting()));

        rolePopularity.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> sb.append("  - ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append(" назначений\n"));

        return sb.toString();
    }
}