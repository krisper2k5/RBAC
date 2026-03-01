package rbac;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerServiceCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser) {
        // user-list
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены.");
                return;
            }
            System.out.println("\n=== Список пользователей ===");
            System.out.printf("%-20s %-30s %-30s\n", "Username", "Full Name", "Email");
            System.out.println("=".repeat(80));
            for (User user : users) {
                System.out.printf("%-20s %-30s %-30s\n",
                        user.username(), user.fullName(), user.email());
            }
            System.out.println("Всего: " + users.size());
        });

        // user-create
        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            try {
                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);
                System.out.println("✓ Пользователь успешно создан: " + user.format());
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // user-view
        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();
            System.out.println("\n=== Информация о пользователе ===");
            System.out.println(user.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("\nНазначенные роли:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.role().name() +
                            " [" + assignment.assignmentType() + "] " +
                            (assignment.isActive() ? "АКТИВНО" : "НЕАКТИВНО"));
                }

                Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
                System.out.println("\nВсе права доступа (" + permissions.size() + "):");
                for (Permission p : permissions) {
                    System.out.println("  - " + p.format());
                }
            } else {
                System.out.println("\nРоли не назначены.");
            }
        });

        // user-update
        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("New Full Name: ");
            String fullName = scanner.nextLine().trim();
            System.out.print("New Email: ");
            String email = scanner.nextLine().trim();

            try {
                system.getUserManager().update(username, fullName, email);
                System.out.println("✓ Данные пользователя обновлены.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // user-delete
        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            System.out.print("Подтвердите удаление (введите 'да'): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("да")) {
                System.out.println("Удаление отменено.");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }

            system.getUserManager().remove(user);
            System.out.println("✓ Пользователь удалён.");
        });

        // user-search
        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("\nВыберите тип фильтра:");
            System.out.println("1. По username (содержит)");
            System.out.println("2. По email (содержит)");
            System.out.println("3. По домену email");
            System.out.println("4. По полному имени (содержит)");
            System.out.print("Выбор: ");

            String choice = scanner.nextLine().trim();
            UserFilter filter = null;

            switch (choice) {
                case "1" -> {
                    System.out.print("Введите часть username: ");
                    filter = UserFilters.byUsernameContains(scanner.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Введите часть email: ");
                    String emailPart = scanner.nextLine().trim();
                    filter = user -> user.email().toLowerCase().contains(emailPart.toLowerCase());
                }
                case "3" -> {
                    System.out.print("Введите домен (например, @company.com): ");
                    filter = UserFilters.byEmailDomain(scanner.nextLine().trim());
                }
                case "4" -> {
                    System.out.print("Введите часть имени: ");
                    filter = UserFilters.byFullNameContains(scanner.nextLine().trim());
                }
                default -> {
                    System.out.println("Неверный выбор.");
                    return;
                }
            }

            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("Пользователи не найдены.");
                return;
            }

            System.out.println("\n=== Результаты поиска (" + results.size() + ") ===");
            for (User user : results) {
                System.out.println("  " + user.format());
            }
        });
    }

    private static void registerRoleCommands(CommandParser parser) {
        // role-list
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Роли не найдены.");
                return;
            }
            System.out.println("\n=== Список ролей ===");
            System.out.printf("%-15s %-10s %-10s\n", "Name", "Permissions", "ID");
            System.out.println("=".repeat(50));
            for (Role role : roles) {
                System.out.printf("%-15s %-10d %-10s\n",
                        role.name(), role.getPermissions().size(), role.id());
            }
            System.out.println("Всего: " + roles.size());
        });

        // role-create
        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.print("Название роли: ");
            String name = scanner.nextLine().trim();
            System.out.print("Описание: ");
            String description = scanner.nextLine().trim();

            try {
                Role role = new Role(name, description);
                system.getRoleManager().add(role);
                System.out.println("✓ Роль создана: " + role.name());

                while (true) {
                    System.out.print("Добавить право? (да/нет): ");
                    String addPermission = scanner.nextLine().trim();
                    if (!addPermission.equalsIgnoreCase("да")) break;

                    System.out.print("Имя права (READ/WRITE/DELETE): ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String resource = scanner.nextLine().trim();
                    System.out.print("Описание: ");
                    String desc = scanner.nextLine().trim();

                    Permission permission = new Permission(permName, resource, desc);
                    system.getRoleManager().addPermissionToRole(role.name(), permission);
                    System.out.println("✓ Право добавлено.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // role-view
        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.println("\n" + roleOpt.get().format());
        });

        // role-update
        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            System.out.print("Имя роли: ");
            String oldName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(oldName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.print("Новое название (оставьте пустым для без изменений): ");
            String newName = scanner.nextLine().trim();
            System.out.print("Новое описание: ");
            String newDesc = scanner.nextLine().trim();

            // Примечание: для полноценного обновления нужно добавить метод в RoleManager
            System.out.println("✓ Обновление роли (требуется доработка RoleManager)");
        });

        // role-delete
        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);

            if (!assignments.isEmpty()) {
                System.out.println("⚠ Роль назначена " + assignments.size() + " пользователям:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.user().username());
                }
                System.out.print("Продолжить удаление? (да/нет): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("да")) {
                    System.out.println("Удаление отменено.");
                    return;
                }
            }

            system.getRoleManager().remove(role);
            System.out.println("✓ Роль удалена.");
        });

        // role-add-permission
        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Имя роли: ");
            String roleName = scanner.nextLine().trim();
            System.out.print("Имя права: ");
            String permName = scanner.nextLine().trim();
            System.out.print("Ресурс: ");
            String resource = scanner.nextLine().trim();
            System.out.print("Описание: ");
            String desc = scanner.nextLine().trim();

            try {
                Permission permission = new Permission(permName, resource, desc);
                system.getRoleManager().addPermissionToRole(roleName, permission);
                System.out.println("✓ Право добавлено к роли.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // role-remove-permission
        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<Permission> permissions = new ArrayList<>(role.getPermissions());

            if (permissions.isEmpty()) {
                System.out.println("У роли нет прав.");
                return;
            }

            System.out.println("\nПрава роли:");
            for (int i = 0; i < permissions.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, permissions.get(i).format());
            }

            System.out.print("Введите номер права для удаления: ");
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (index >= 0 && index < permissions.size()) {
                system.getRoleManager().removePermissionFromRole(roleName, permissions.get(index));
                System.out.println("✓ Право удалено.");
            } else {
                System.out.println("Неверный номер.");
            }
        });

        // role-search
        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.println("\nВыберите тип поиска:");
            System.out.println("1. По имени (содержит)");
            System.out.println("2. По наличию права");
            System.out.println("3. По минимальному количеству прав");
            System.out.print("Выбор: ");

            String choice = scanner.nextLine().trim();
            RoleFilter filter = null;

            switch (choice) {
                case "1" -> {
                    System.out.print("Введите часть имени: ");
                    filter = RoleFilters.byNameContains(scanner.nextLine().trim());
                }
                case "2" -> {
                    System.out.print("Имя права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String resource = scanner.nextLine().trim();
                    filter = RoleFilters.hasPermission(permName, resource);
                }
                case "3" -> {
                    System.out.print("Минимальное количество прав: ");
                    int n = Integer.parseInt(scanner.nextLine().trim());
                    filter = RoleFilters.hasAtLeastNPermissions(n);
                }
                default -> {
                    System.out.println("Неверный выбор.");
                    return;
                }
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("Роли не найдены.");
                return;
            }

            System.out.println("\n=== Результаты поиска (" + results.size() + ") ===");
            for (Role role : results) {
                System.out.println("  " + role.name() + " (" + role.getPermissions().size() + " прав)");
            }
        });
    }

    private static void registerAssignmentCommands(CommandParser parser) {
        // assign-role
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            List<Role> roles = system.getRoleManager().findAll();
            System.out.println("\nДоступные роли:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, roles.get(i).name());
            }

            System.out.print("Выберите роль (номер): ");
            int roleIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (roleIndex < 0 || roleIndex >= roles.size()) {
                System.out.println("Неверный выбор.");
                return;
            }

            Role role = roles.get(roleIndex);

            System.out.print("Тип назначения (1-постоянное/2-временное): ");
            String type = scanner.nextLine().trim();

            System.out.print("Причина назначения: ");
            String reason = scanner.nextLine().trim();

            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                if (type.equals("2")) {
                    System.out.print("Дата истечения (ГГГГ-ММ-ДД ЧЧ:ММ): ");
                    String expiresAt = scanner.nextLine().trim();
                    TemporaryAssignment assignment = new TemporaryAssignment(
                            userOpt.get(), role, metadata, expiresAt);
                    system.getAssignmentManager().add(assignment);
                    System.out.println("✓ Временное назначение создано: " + assignment.assignmentId());
                } else {
                    PermanentAssignment assignment = new PermanentAssignment(
                            userOpt.get(), role, metadata);
                    system.getAssignmentManager().add(assignment);
                    System.out.println("✓ Постоянное назначение создано: " + assignment.assignmentId());
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        // revoke-role
        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager()
                    .findByUser(userOpt.get()).stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());

            if (assignments.isEmpty()) {
                System.out.println("Нет активных назначений.");
                return;
            }

            System.out.println("\nАктивные назначения:");
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment a = assignments.get(i);
                System.out.printf("  %d. %s - %s [%s]\n",
                        i + 1, a.role().name(), a.assignmentType(), a.assignmentId());
            }

            System.out.print("Выберите назначение (номер): ");
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (index >= 0 && index < assignments.size()) {
                RoleAssignment assignment = assignments.get(index);
                system.getAssignmentManager().revokeAssignment(assignment.assignmentId());
                System.out.println("✓ Назначение отозвано.");
            } else {
                System.out.println("Неверный выбор.");
            }
        });

        // assignment-list
        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("Назначения не найдены.");
                return;
            }

            System.out.println("\n=== Все назначения ===");
            System.out.printf("%-15s %-15s %-12s %-10s %-20s\n",
                    "Username", "Role", "Type", "Status", "Assigned At");
            System.out.println("=".repeat(80));

            for (RoleAssignment a : assignments) {
                System.out.printf("%-15s %-15s %-12s %-10s %-20s\n",
                        a.user().username(),
                        a.role().name(),
                        a.assignmentType(),
                        a.isActive() ? "АКТИВНО" : "НЕАКТИВНО",
                        a.metadata().assignedAt());
            }
            System.out.println("Всего: " + assignments.size());
        });

        // assignment-list-user
        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя",
                (scanner, system) -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> userOpt = system.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("Пользователь не найден.");
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager()
                            .findByUser(userOpt.get());

                    if (assignments.isEmpty()) {
                        System.out.println("Назначений не найдено.");
                        return;
                    }

                    System.out.println("\n=== Назначения пользователя " + username + " ===");
                    for (RoleAssignment a : assignments) {
                        System.out.println(a.summary());
                        System.out.println();
                    }
                });

        // assignment-list-role
        parser.registerCommand("assignment-list-role", "Список пользователей с конкретной ролью",
                (scanner, system) -> {
                    System.out.print("Имя роли: ");
                    String roleName = scanner.nextLine().trim();

                    Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
                    if (roleOpt.isEmpty()) {
                        System.out.println("Роль не найдена.");
                        return;
                    }

                    List<RoleAssignment> assignments = system.getAssignmentManager()
                            .findByRole(roleOpt.get());

                    if (assignments.isEmpty()) {
                        System.out.println("Пользователей с этой ролью не найдено.");
                        return;
                    }

                    System.out.println("\n=== Пользователи с ролью " + roleName + " ===");
                    for (RoleAssignment a : assignments) {
                        System.out.println("  " + a.user().format() +
                                " [" + (a.isActive() ? "АКТИВНО" : "НЕАКТИВНО") + "]");
                    }
                });

        // assignment-active
        parser.registerCommand("assignment-active", "Активные назначения", (scanner, system) -> {
            List<RoleAssignment> assignments = system.getAssignmentManager().getActiveAssignments();
            if (assignments.isEmpty()) {
                System.out.println("Активных назначений не найдено.");
                return;
            }
            System.out.println("\n=== Активные назначения (" + assignments.size() + ") ===");
            for (RoleAssignment a : assignments) {
                System.out.printf("  %s -> %s\n", a.user().username(), a.role().name());
            }
        });

        // assignment-expired
        parser.registerCommand("assignment-expired", "Истёкшие временные назначения",
                (scanner, system) -> {
                    List<RoleAssignment> assignments = system.getAssignmentManager()
                            .getExpiredAssignments();

                    if (assignments.isEmpty()) {
                        System.out.println("Истёкших назначений не найдено.");
                        return;
                    }

                    System.out.println("\n=== Истёкшие назначения (" + assignments.size() + ") ===");
                    for (RoleAssignment a : assignments) {
                        System.out.println("  " + a.user().username() + " -> " + a.role().name());
                    }
                });

        // assignment-extend
        parser.registerCommand("assignment-extend", "Продлить временное назначение",
                (scanner, system) -> {
                    System.out.print("Assignment ID: ");
                    String assignmentId = scanner.nextLine().trim();
                    System.out.print("Новая дата истечения (ГГГГ-ММ-ДД ЧЧ:ММ): ");
                    String newDate = scanner.nextLine().trim();

                    try {
                        system.getAssignmentManager().extendTemporaryAssignment(assignmentId, newDate);
                        System.out.println("✓ Назначение продлено.");
                    } catch (Exception e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                });

        // assignment-search
        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам",
                (scanner, system) -> {
                    System.out.println("\nВыберите фильтр:");
                    System.out.println("1. По пользователю");
                    System.out.println("2. По роли");
                    System.out.println("3. По типу (постоянное/временное)");
                    System.out.println("4. По статусу (активное/неактивное)");
                    System.out.print("Выбор: ");

                    String choice = scanner.nextLine().trim();
                    AssignmentFilter filter = null;

                    switch (choice) {
                        case "1" -> {
                            System.out.print("Username: ");
                            filter = AssignmentFilters.byUsername(scanner.nextLine().trim());
                        }
                        case "2" -> {
                            System.out.print("Имя роли: ");
                            filter = AssignmentFilters.byRoleName(scanner.nextLine().trim());
                        }
                        case "3" -> {
                            System.out.print("Тип (ПОСТОЯННОЕ/ВРЕМЕННОЕ): ");
                            filter = AssignmentFilters.byType(scanner.nextLine().trim());
                        }
                        case "4" -> {
                            System.out.print("Статус (active/inactive): ");
                            String status = scanner.nextLine().trim();
                            filter = status.equalsIgnoreCase("active") ?
                                    AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
                        }
                        default -> {
                            System.out.println("Неверный выбор.");
                            return;
                        }
                    }

                    List<RoleAssignment> results = system.getAssignmentManager()
                            .findByFilter(filter);

                    if (results.isEmpty()) {
                        System.out.println("Назначения не найдены.");
                        return;
                    }

                    System.out.println("\n=== Результаты поиска (" + results.size() + ") ===");
                    for (RoleAssignment a : results) {
                        System.out.println("  " + a.user().username() + " -> " + a.role().name() +
                                " [" + a.assignmentType() + "] " +
                                (a.isActive() ? "АКТИВНО" : "НЕАКТИВНО"));
                    }
                });
    }

    private static void registerPermissionCommands(CommandParser parser) {
        // permissions-user
        parser.registerCommand("permissions-user", "Все права конкретного пользователя",
                (scanner, system) -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();

                    Optional<User> userOpt = system.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("Пользователь не найден.");
                        return;
                    }

                    Set<Permission> permissions = system.getAssignmentManager()
                            .getUserPermissions(userOpt.get());

                    if (permissions.isEmpty()) {
                        System.out.println("У пользователя нет прав.");
                        return;
                    }

                    System.out.println("\n=== Права пользователя " + username + " ===");
                    Map<String, List<Permission>> byResource = permissions.stream()
                            .collect(Collectors.groupingBy(Permission::resource));

                    for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                        System.out.println("\nРесурс: " + entry.getKey());
                        for (Permission p : entry.getValue()) {
                            System.out.println("  - " + p.name() + ": " + p.description());
                        }
                    }
                });

        // permissions-check
        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя",
                (scanner, system) -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Имя права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Ресурс: ");
                    String resource = scanner.nextLine().trim();

                    Optional<User> userOpt = system.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("Пользователь не найден.");
                        return;
                    }

                    boolean hasPermission = system.getAssignmentManager()
                            .userHasPermission(userOpt.get(), permName, resource);

                    if (hasPermission) {
                        System.out.println("✓ Право " + permName + " на " + resource + " ЕСТЬ");

                        // Найти из какой роли
                        List<RoleAssignment> assignments = system.getAssignmentManager()
                                .findByUser(userOpt.get());
                        for (RoleAssignment a : assignments) {
                            if (a.role().hasPermission(permName, resource)) {
                                System.out.println("  Из роли: " + a.role().name());
                            }
                        }
                    } else {
                        System.out.println("✗ Право " + permName + " на " + resource + " ОТСУТСТВУЕТ");
                    }
                });
    }

    private static void registerServiceCommands(CommandParser parser) {
        // help
        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        // stats
        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        // clear
        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Экран очищен.");
        });

        // exit
        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Сохранить данные перед выходом? (да/нет): ");
            String save = scanner.nextLine().trim();
            if (save.equalsIgnoreCase("да")) {
                System.out.println("✓ Данные сохранены (функционал требует доработки).");
            }
            System.out.println("Выход из программы...");
            System.exit(0);
        });

        // save
        parser.registerCommand("save", "Сохранить данные в файл", (scanner, system) -> {
            System.out.println("✓ Данные сохранены (функционал требует доработки).");
        });

        // load
        parser.registerCommand("load", "Загрузить данные из файла", (scanner, system) -> {
            System.out.println("✓ Данные загружены (функционал требует доработки).");
        });
    }
}