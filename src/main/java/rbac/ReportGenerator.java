package rbac;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователи не найдены.\n");
            return sb.toString();
        }

        List<String[]> rows = new ArrayList<>();
        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            String roles = assignments.isEmpty() ? "не назначено" :
                    assignments.stream()
                            .map(a -> a.role().name())
                            .collect(Collectors.joining(", "));

            rows.add(new String[] {
                    user.username(),
                    user.fullName(),
                    user.email(),
                    roles
            });
        }

        sb.append(FormatUtils.formatTable(
                new String[] {"Username", "Full Name", "Email", "Роли"},
                rows
        ));

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("ОТЧЁТ ПО РОЛЯМ"));

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("Роли не найдены.\n");
            return sb.toString();
        }

        List<String[]> rows = new ArrayList<>();
        for (Role role : roles) {
            int userCount = assignmentManager.findByRole(role).size();
            rows.add(new String[] {
                    role.name(),
                    String.valueOf(userCount),
                    String.valueOf(role.getPermissions().size()),
                    FormatUtils.truncate(role.description(), 30)
            });
        }

        sb.append(FormatUtils.formatTable(
                new String[] {"Роль", "Пользователей", "Прав", "Описание"},
                rows
        ));

        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("МАТРИЦА ПРАВ ДОСТУПА"));

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

        List<String[]> rows = new ArrayList<>();
        for (User user : userManager.findAll()) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            List<String> row = new ArrayList<>();
            row.add(user.username());

            for (String res : resources) {
                boolean hasRead = perms.stream().anyMatch(p ->
                        p.resource().equals(res) && p.name().equals("READ"));
                boolean hasWrite = perms.stream().anyMatch(p ->
                        p.resource().equals(res) && p.name().equals("WRITE"));

                if (hasRead && hasWrite) row.add("R+W");
                else if (hasRead) row.add("R");
                else if (hasWrite) row.add("W");
                else row.add("-");
            }
            rows.add(row.toArray(new String[0]));
        }

        String[] headers = new String[resources.size() + 1];
        headers[0] = "Пользователь";
        int i = 1;
        for (String res : resources) {
            headers[i++] = res.toUpperCase();
        }

        sb.append(FormatUtils.formatTable(headers, rows));

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(report);
            System.out.println("✓ Отчёт сохранён в " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка экспорта: " + e.getMessage());
        }
    }
}