package rbac;

import java.util.regex.Pattern;

//Запись пользователя с валидацией данных
public record User(String username, String fullName, String email) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public User {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым или null");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Имя пользователя должно содержать только латинские буквы, цифры, подчёркивания и быть длиной 3-20 символов"
            );
        }
        username = username.trim();

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя не может быть пустым или null");
        }
        fullName = fullName.trim();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым или null");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email должен содержать '@' и точку после '@'");
        }
        email = email.trim().toLowerCase();
    }

    //Статический метод для валидации и создания пользователя
    public static User validate(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }

    //Форматированный вывод пользователя
    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}
