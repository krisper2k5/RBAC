package rbac;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerReportCommands(parser);  // ← ДОБАВЛЕНО!
        registerServiceCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                ConsoleUtils.printWarning("Пользователи не найдены.");
                return;
            }
            List<String[]> rows = new ArrayList<>();
            for (User user : users) {
                rows.add(new String[] { user.username(), user.fullName(), user.email() });
            }
            System.out.println(FormatUtils.formatTable(
                    new String[] {"Username", "Full Name", "Email"}, rows));
            ConsoleUtils.printSuccess("Всего: " + users.size());
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true, ValidationUtils::isValidUsername);
            String fullName = ConsoleUtils.promptString("Full Name", true);
            String email = ConsoleUtils.promptString("Email", true, ValidationUtils::isValidEmail);
            try {
                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);
                system.getAuditLog().log("USER_CREATE", system.getCurrentUser(), username, "Создание пользователя");
                ConsoleUtils.printSuccess("Пользователь успешно создан: " + user.format());
            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            User user = userOpt.get();
            System.out.println(FormatUtils.formatBox(user.format()));
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("\nНазначенные роли:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.role().name() +
                            " [" + assignment.assignmentType() + "] " +
                            (assignment.isActive() ? "АКТИВНО" : "НЕАКТИВНО"));
                }
            } else {
                ConsoleUtils.printWarning("Роли не назначены.");
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            if (!ConsoleUtils.promptYesNo("Подтвердите удаление")) {
                ConsoleUtils.printWarning("Удаление отменено.");
                return;
            }
            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }
            system.getUserManager().remove(user);
            system.getAuditLog().log("USER_DELETE", system.getCurrentUser(), username, "Удаление пользователя");
            ConsoleUtils.printSuccess("Пользователь удалён.");
        });
    }

    private static void registerRoleCommands(CommandParser parser) {
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                ConsoleUtils.printWarning("Роли не найдены.");
                return;
            }
            List<String[]> rows = new ArrayList<>();
            for (Role role : roles) {
                rows.add(new String[] { role.name(), String.valueOf(role.getPermissions().size()), role.id() });
            }
            System.out.println(FormatUtils.formatTable(
                    new String[] {"Name", "Permissions", "ID"}, rows));
            ConsoleUtils.printSuccess("Всего: " + roles.size());
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            String name = ConsoleUtils.promptString("Название роли", true);
            String description = ConsoleUtils.promptString("Описание", true);
            try {
                Role role = new Role(name, description);
                system.getRoleManager().add(role);
                system.getAuditLog().log("ROLE_CREATE", system.getCurrentUser(), name, "Создание роли");
                ConsoleUtils.printSuccess("Роль создана: " + role.name());
                while (ConsoleUtils.promptYesNo("Добавить право?")) {
                    String permName = ConsoleUtils.promptString("Имя права (READ/WRITE/DELETE)", true);
                    String resource = ConsoleUtils.promptString("Ресурс", true);
                    String desc = ConsoleUtils.promptString("Описание", true);
                    Permission permission = new Permission(permName, resource, desc);
                    system.getRoleManager().addPermissionToRole(role.name(), permission);
                    ConsoleUtils.printSuccess("Право добавлено.");
                }
            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString("Имя роли", true);
            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена.");
                return;
            }
            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                ConsoleUtils.printWarning("Роль назначена " + assignments.size() + " пользователям:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.user().username());
                }
                if (!ConsoleUtils.promptYesNo("Продолжить удаление?")) {
                    ConsoleUtils.printWarning("Удаление отменено.");
                    return;
                }
            }
            system.getRoleManager().remove(role);
            system.getAuditLog().log("ROLE_DELETE", system.getCurrentUser(), roleName, "Удаление роли");
            ConsoleUtils.printSuccess("Роль удалена.");
        });
    }

    private static void registerAssignmentCommands(CommandParser parser) {
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            List<Role> roles = system.getRoleManager().findAll();
            Role role = ConsoleUtils.promptChoice("Выберите роль:", roles);
            String type = ConsoleUtils.promptChoice("Тип назначения", Arrays.asList("ПОСТОЯННОЕ", "ВРЕМЕННОЕ")).toString();
            String reason = ConsoleUtils.promptString("Причина назначения", true);
            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);
            try {
                if (type.equals("ВРЕМЕННОЕ")) {
                    String expiresAt = ConsoleUtils.promptString("Дата истечения (ГГГГ-ММ-ДД ЧЧ:ММ)", true, ValidationUtils::isValidDate);
                    TemporaryAssignment assignment = new TemporaryAssignment(userOpt.get(), role, metadata, expiresAt);
                    system.getAssignmentManager().add(assignment);
                    ConsoleUtils.printSuccess("Временное назначение создано: " + assignment.assignmentId());
                } else {
                    PermanentAssignment assignment = new PermanentAssignment(userOpt.get(), role, metadata);
                    system.getAssignmentManager().add(assignment);
                    ConsoleUtils.printSuccess("Постоянное назначение создано: " + assignment.assignmentId());
                }
                system.getAuditLog().log("ROLE_ASSIGN", system.getCurrentUser(), username, "Назначена роль " + role.name());
            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager()
                    .findByUser(userOpt.get()).stream()
                    .filter(RoleAssignment::isActive)
                    .toList();
            if (assignments.isEmpty()) {
                ConsoleUtils.printWarning("Нет активных назначений.");
                return;
            }
            RoleAssignment selected = ConsoleUtils.promptChoice("Выберите назначение для отзыва:", assignments);
            system.getAssignmentManager().revokeAssignment(selected.assignmentId());
            system.getAuditLog().log("ROLE_REVOKE", system.getCurrentUser(), username, "Отозвана роль " + selected.role().name());
            ConsoleUtils.printSuccess("Назначение отозвано.");
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("Назначения не найдены.");
                return;
            }
            System.out.println("\n=== Все назначения ===");
            for (RoleAssignment a : assignments) {
                System.out.printf("  %s -> %s [%s] %s\n", a.user().username(), a.role().name(), a.assignmentType(), a.isActive() ? "АКТИВНО" : "НЕАКТИВНО");
            }
            System.out.println("Всего: " + assignments.size());
        });

        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().getActiveAssignments();
            if (assignments.isEmpty()) {
                ConsoleUtils.printWarning("Активных назначений не найдено.");
                return;
            }
            System.out.println("\n=== Активные назначения (" + assignments.size() + ") ===");
            for (RoleAssignment a : assignments) {
                System.out.println("  " + a.user().username() + " -> " + a.role().name());
            }
        });
    }

    private static void registerPermissionCommands(CommandParser parser) {
        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(userOpt.get());
            if (permissions.isEmpty()) {
                ConsoleUtils.printWarning("У пользователя нет прав.");
                return;
            }
            System.out.println(FormatUtils.formatHeader("ПРАВА ПОЛЬЗОВАТЕЛЯ " + username.toUpperCase()));
            Map<String, List<Permission>> byResource = permissions.stream().collect(Collectors.groupingBy(Permission::resource));
            for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                System.out.println("\nРесурс: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("  - " + p.name() + ": " + p.description());
                }
            }
        });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString("Username", true);
            String permName = ConsoleUtils.promptString("Имя права", true);
            String resource = ConsoleUtils.promptString("Ресурс", true);
            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден.");
                return;
            }
            boolean hasPermission = system.getAssignmentManager().userHasPermission(userOpt.get(), permName, resource);
            if (hasPermission) {
                ConsoleUtils.printSuccess("Право " + permName + " на " + resource + " ЕСТЬ");
            } else {
                ConsoleUtils.printError("Право " + permName + " на " + resource + " ОТСУТСТВУЕТ");
            }
        });
    }

    // === ОТЧЁТЫ И АУДИТ ===
    private static void registerReportCommands(CommandParser parser) {
        parser.registerCommand("report-users", "Отчёт по пользователям", (scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateUserReport(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
            if (ConsoleUtils.promptYesNo("Сохранить в файл?")) {
                String filename = ConsoleUtils.promptString("Имя файла", true);
                generator.exportToFile(report, filename);
            }
        });

        parser.registerCommand("report-roles", "Отчёт по ролям", (scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateRoleReport(system.getRoleManager(), system.getAssignmentManager());
            System.out.println(report);
        });

        parser.registerCommand("report-matrix", "Матрица прав доступа", (scanner, system) -> {
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generatePermissionMatrix(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
        });

        parser.registerCommand("audit-log", "Просмотр лога аудита", (scanner, system) -> {
            system.getAuditLog().printLog();
            if (ConsoleUtils.promptYesNo("Сохранить лог в файл?")) {
                String filename = ConsoleUtils.promptString("Имя файла", true);
                system.getAuditLog().saveToFile(filename);
            }
        });
    }

    private static void registerServiceCommands(CommandParser parser) {
        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printSuccess("Экран очищен.");
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            if (ConsoleUtils.promptYesNo("Сохранить данные перед выходом?")) {
                ConsoleUtils.printWarning("Функция сохранения требует доработки.");
            }
            ConsoleUtils.printSuccess("Выход из программы...");
            System.exit(0);
        });
    }
}