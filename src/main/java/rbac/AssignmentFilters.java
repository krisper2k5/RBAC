package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentFilters {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentFilter byUser(User user) {
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equalsIgnoreCase(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().name().equalsIgnoreCase(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equalsIgnoreCase(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDateTime parsedDate = LocalDateTime.parse(date, FORMATTER);
        return assignment -> {
            LocalDateTime assignedAt = LocalDateTime.parse(assignment.metadata().assignedAt(), FORMATTER);
            return assignedAt.isAfter(parsedDate);
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDateTime parsedDate = LocalDateTime.parse(date, FORMATTER);
        return assignment -> {
            if (assignment instanceof TemporaryAssignment temp) {
                return temp.parseExpiryDate().isBefore(parsedDate);
            }
            return false;
        };
    }
}