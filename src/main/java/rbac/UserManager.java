package rbac;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override public void add(User user) {
        if (users.containsKey(user.username())) throw new IllegalArgumentException("Пользователь с таким username уже существует");
        users.put(user.username(), user);
    }
    @Override public boolean remove(User user) { return users.remove(user.username()) != null; }
    @Override public Optional<User> findById(String id) { return Optional.ofNullable(users.get(id)); }
    @Override public List<User> findAll() { return new ArrayList<>(users.values()); }
    @Override public int count() { return users.size(); }
    @Override public void clear() { users.clear(); }

    public Optional<User> findByUsername(String username) { return Optional.ofNullable(users.get(username)); }
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        return users.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        return users.values().stream()
                .filter(filter == null ? u -> true : filter::test)
                .sorted(sorter != null ? sorter : Comparator.comparing(User::username))
                .collect(Collectors.toList());
    }

    public boolean exists(String username) { return users.containsKey(username); }

    public void update(String username, String newFullName, String newEmail) {
        User user = users.get(username);
        if (user == null) throw new IllegalArgumentException("Пользователь не найден");
        users.put(username, new User(username, newFullName, newEmail));
    }
}