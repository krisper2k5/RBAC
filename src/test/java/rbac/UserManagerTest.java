package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void testAddAndGetUser() {
        User user = new User("ilya_kormilitsyn", "Ilya Kormilitsyn", "kormilitsyn@mail.ru");
        userManager.add(user);

        Optional<User> found = userManager.findById("ilya_kormilitsyn");
        assertTrue(found.isPresent());
        assertEquals("ilya_kormilitsyn", found.get().username());
    }

    @Test
    void testFindByUsername() {
        User user = new User("jane_klod", "Jane Klod", "jane@example.com");
        userManager.add(user);

        Optional<User> found = userManager.findByUsername("jane_klod");
        assertTrue(found.isPresent());
        assertEquals("jane@example.com", found.get().email());
    }

    @Test
    void testFindByEmail() {
        User user = new User("alice", "Alice Smith", "alice@example.com");
        userManager.add(user);

        Optional<User> found = userManager.findByEmail("alice@example.com");
        assertTrue(found.isPresent());
        assertEquals("Alice Smith", found.get().fullName());
    }

    @Test
    void testFilterByUsernameContains() {
        userManager.add(new User("user1", "User One", "user1@example.com"));
        userManager.add(new User("user2", "User Two", "user2@example.com"));

        List<User> filtered = userManager.findByFilter(UserFilters.byUsernameContains("user"));
        assertEquals(2, filtered.size());
    }

    @Test
    void testSortByUsername() {
        userManager.add(new User("b_user", "B User", "b@example.com"));
        userManager.add(new User("a_user", "A User", "a@example.com"));

        List<User> sorted = userManager.findAll(null, UserSorters.byUsername());
        assertEquals("a_user", sorted.get(0).username());
        assertEquals("b_user", sorted.get(1).username());
    }

    @Test
    void testUpdateUser() {
        userManager.add(new User("test_user", "Old Name", "old@example.com"));
        userManager.update("test_user", "New Name", "new@example.com");

        Optional<User> updated = userManager.findById("test_user");
        assertTrue(updated.isPresent());
        assertEquals("New Name", updated.get().fullName());
        assertEquals("new@example.com", updated.get().email());
    }

    @Test
    void testRemoveUser() {
        User user = new User("remove_me", "Remove Me", "remove@example.com");
        userManager.add(user);
        assertTrue(userManager.remove(user));
        assertFalse(userManager.findById("remove_me").isPresent());
    }
}