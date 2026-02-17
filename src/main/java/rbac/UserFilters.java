package rbac;

public class UserFilters {
    public static UserFilter byUsername(String username) {
        return user -> user.username().equalsIgnoreCase(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        return user -> user.username().toLowerCase().contains(substring.toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        return user -> user.email().equalsIgnoreCase(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return user -> user.email().toLowerCase().endsWith(domain.toLowerCase());
    }

    public static UserFilter byFullNameContains(String substring) {
        return user -> user.fullName().toLowerCase().contains(substring.toLowerCase());
    }
}