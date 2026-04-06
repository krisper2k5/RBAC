package rbac;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();
    private final Object roleLock = new Object();

    @Override public void add(Role role) {
        synchronized (roleLock) {
            if (rolesByName.containsKey(role.name())) throw new IllegalArgumentException("Роль с таким именем уже существует");
            rolesById.put(role.id(), role);
            rolesByName.put(role.name(), role);
        }
    }
    @Override public boolean remove(Role role) {
        synchronized (roleLock) {
            rolesByName.remove(role.name());
            return rolesById.remove(role.id()) != null;
        }
    }
    @Override public Optional<Role> findById(String id) { return Optional.ofNullable(rolesById.get(id)); }
    @Override public List<Role> findAll() { return new ArrayList<>(rolesById.values()); }
    @Override public int count() { return rolesById.size(); }
    @Override public void clear() { synchronized (roleLock) { rolesById.clear(); rolesByName.clear(); } }

    public Optional<Role> findByName(String name) { return Optional.ofNullable(rolesByName.get(name)); }

    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        return rolesById.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(role -> filter == null || filter.test(role))
                .sorted(sorter != null ? sorter : Comparator.comparing(Role::name))
                .collect(Collectors.toList());
    }

    public boolean exists(String name) { return rolesByName.containsKey(name); }
    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) throw new IllegalArgumentException("Роль не найдена");
        role.addPermission(permission);
    }
    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) throw new IllegalArgumentException("Роль не найдена");
        role.removePermission(permission);
    }
    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }
}