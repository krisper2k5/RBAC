package rbac;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    private final Object assignmentLock = new Object();

    @Override public void add(RoleAssignment assignment) {
        if (assignments.containsKey(assignment.assignmentId())) throw new IllegalArgumentException("Назначение с таким ID уже существует");
        assignments.put(assignment.assignmentId(), assignment);
    }
    @Override public boolean remove(RoleAssignment assignment) { return assignments.remove(assignment.assignmentId()) != null; }
    @Override public Optional<RoleAssignment> findById(String id) { return Optional.ofNullable(assignments.get(id)); }
    @Override public List<RoleAssignment> findAll() { return new ArrayList<>(assignments.values()); }
    @Override public int count() { return assignments.size(); }
    @Override public void clear() { assignments.clear(); }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }
    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        return assignments.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }
    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role));
    }
    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .anyMatch(a -> a.role().hasPermission(permissionName, resource));
    }
    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        synchronized (assignmentLock) {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) return;
            if (assignment instanceof PermanentAssignment pa) pa.revoke();
            else assignments.remove(assignmentId);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        synchronized (assignmentLock) {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) return;
            if (!(assignment instanceof TemporaryAssignment ta)) throw new IllegalArgumentException("Назначение не является временным");
            ta.extend(newExpirationDate);
        }
    }
}