package rbac;
import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private final BackgroundExecutor executor;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.auditLog = new AuditLog();
        this.executor = BackgroundExecutor.getInstance();
        this.currentUser = "system";
    }

    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }
    public BackgroundExecutor getExecutor() { return executor; }

    public void setCurrentUser(String username) { this.currentUser = username; }
    public String getCurrentUser() { return currentUser; }

    public void initialize() {
        auditLog.log("SYSTEM_INIT", "system", "RBAC", "Инициализация системы");
        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");
        Permission readReports = new Permission("READ", "reports", "Просмотр отчётов");
        Permission writeReports = new Permission("WRITE", "reports", "Редактирование отчётов");
        Permission readSettings = new Permission("READ", "settings", "Просмотр настроек");
        Permission writeSettings = new Permission("WRITE", "settings", "Изменение настроек");

        Role admin = new Role("Admin", "Полный доступ ко всем функциям системы");
        admin.addPermission(readUsers); admin.addPermission(writeUsers); admin.addPermission(deleteUsers);
        admin.addPermission(readReports); admin.addPermission(writeReports);
        admin.addPermission(readSettings); admin.addPermission(writeSettings);
        roleManager.add(admin);

        Role manager = new Role("Manager", "Управление отчётами и пользователями");
        manager.addPermission(readUsers); manager.addPermission(writeUsers);
        manager.addPermission(readReports); manager.addPermission(writeReports);
        roleManager.add(manager);

        Role viewer = new Role("Viewer", "Только для чтения");
        viewer.addPermission(readUsers); viewer.addPermission(readReports); viewer.addPermission(readSettings);
        roleManager.add(viewer);

        User adminUser = User.validate("admin", "Администратор Системы", "admin@company.com");
        userManager.add(adminUser);
        auditLog.log("USER_CREATE", "system", "admin", "Создан пользователь admin");

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Инициализация системы");
        PermanentAssignment assignment = new PermanentAssignment(adminUser, admin, meta);
        assignmentManager.add(assignment);
        auditLog.log("ROLE_ASSIGN", "system", "admin", "Назначена роль Admin");
        setCurrentUser("admin");
    }

    // асинхронная генерация отчёта
    public void generateUserReportAsync(String filename) {
        executor.submit(() -> {
            System.out.println("Генерация отчёта в фоне...");
            ReportGenerator gen = new ReportGenerator();
            String report = gen.generateUserReport(userManager, assignmentManager);
            gen.exportToFile(report, filename);
            auditLog.logAsync("REPORT_GENERATED", currentUser, filename, "Отчёт сгенерирован асинхронно");
        });
    }

    // асинхронное сохранение данных
    public void saveDataAsync(String filename) {
        executor.submit(() -> {
            System.out.println("Сохранение данных в фоне...");
            try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
                out.println("USERS: " + userManager.count());
                out.println("ROLES: " + roleManager.count());
                out.println("ASSIGNMENTS: " + assignmentManager.count());
                System.out.println("✓ Данные сохранены в " + filename);
                auditLog.logAsync("DATA_SAVED", currentUser, filename, "Асинхронный бэкап");
            } catch (Exception e) {
                System.err.println("Ошибка сохранения: " + e.getMessage());
            }
        });
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

        double avgRolesPerUser = userCount > 0 ? (double) assignmentCount / userCount : 0;
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