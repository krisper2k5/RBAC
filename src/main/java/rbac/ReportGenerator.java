package rbac;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    // Отчёт по пользователям с их ролями

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены.\n");
            return sb.toString();
        }

        for (User user : users) {
            sb.append(" ").append(user.format()).append("\n");

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            if (assignments.isEmpty()) {
                sb.append("   └─ Роли: не назначено\n");
            } else {
                sb.append("   └─ Роли:\n");
                for (RoleAssignment a : assignments) {
                    sb.append(String.format("      • %s [%s] %s\n",
                            a.role().name(),
                            a.assignmentType(),
                            a.isActive() ? "✓" : "✗"));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Отчёт по ролям с количеством пользователей

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ОТЧЁТ ПО РОЛЯМ ===\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("Роли не найдены.\n");
            return sb.toString();
        }

        for (Role role : roles) {
            int userCount = assignmentManager.findByRole(role).size();
            sb.append(String.format(" %s (%d пользователей)\n", role.name(), userCount));
            sb.append("   Описание: ").append(role.description()).append("\n");
            sb.append("   Права: ").append(role.getPermissions().size()).append("\n");
            for (Permission p : role.getPermissions()) {
                sb.append("      • ").append(p.format()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Матрица прав: пользователи × ресурсы

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== МАТРИЦА ПРАВ ДОСТУПА ===\n\n");

        // Собираем все уникальные ресурсы
        Set<String> resources = new TreeSet<>();
        for (User user : userManager.findAll()) {
            for (Permission p : assignmentManager.getUserPermissions(user)) {
                resources.add(p.resource());
            }
        }

        if (resources.isEmpty()) {
            sb.append("Права доступа не найдены.\n");
            return sb.toString();
        }

        // Заголовок таблицы
        sb.append(String.format("%-20s", "Пользователь"));
        for (String res : resources) {
            sb.append(String.format("%-12s", res.toUpperCase()));
        }
        sb.append("\n").append("─".repeat(20 + resources.size() * 12)).append("\n");

        // Строки таблицы
        for (User user : userManager.findAll()) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            sb.append(String.format("%-20s", user.username()));
            for (String res : resources) {
                boolean hasRead = perms.stream().anyMatch(p ->
                        p.resource().equals(res) && p.name().equals("READ"));
                boolean hasWrite = perms.stream().anyMatch(p ->
                        p.resource().equals(res) && p.name().equals("WRITE"));

                if (hasRead && hasWrite) {
                    sb.append(String.format("%-12s", "R+W"));
                } else if (hasRead) {
                    sb.append(String.format("%-12s", "R"));
                } else if (hasWrite) {
                    sb.append(String.format("%-12s", "W"));
                } else {
                    sb.append(String.format("%-12s", "-"));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Экспорт отчёта в файл
    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(report);
            System.out.println("✓ Отчёт сохранён в " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка экспорта: " + e.getMessage());
        }
    }
}