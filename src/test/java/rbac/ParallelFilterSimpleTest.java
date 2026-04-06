package rbac;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Минимальный тест параллельной фильтрации")
class ParallelFilterSimpleTest {

    @Test
    void findByFilterParallelReturnsCorrectData() {
        UserManager um = new UserManager();
        um.add(new User("user_01", "First User", "u1@test.com"));
        um.add(new User("user_02", "Second User", "u2@test.com"));
        um.add(new User("admin_sys", "Administrator", "adm@test.com"));

        List<User> result = um.findByFilterParallel(UserFilters.byUsernameContains("user_"));

        assertEquals(2, result.size(), "Должно быть найдено ровно 2 пользователя");
        assertTrue(result.stream().allMatch(u -> u.username().startsWith("user_")));
    }
}