package ru.protal.b2b.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationInfo {

    private static final String NOT_NULL ="Поле должно быть заполнено";
    public static final String NAMES_PATTERN = "^(\\w|[а-я]|[А-Я]){2,35}$";
    public static final String LOGIN_PATTERN = "^\\w{2,35}$";


    @NotNull(message = NOT_NULL)
    @Pattern(regexp = LOGIN_PATTERN, message = "login не должен превышать 35 символов")
    String login;

    @NotNull(message = NOT_NULL)
    @Email(message = "Укажите корректный e-mail.")
    String email;

    @NotNull(message = NOT_NULL)
    @Pattern(regexp = NAMES_PATTERN, message = "имя не должно превышать 35 символов")
    String firstName;

    @NotNull(message = NOT_NULL)
    @Pattern(regexp = NAMES_PATTERN, message = "отчество не должно превышать 35 символов")
    String middleName;

    @NotNull(message = NOT_NULL)
    @Pattern(regexp = NAMES_PATTERN, message = "фамилия не должна превышать 35 символов")
    String lastName;

    @NotNull(message = NOT_NULL)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
             message = "Пароль должен содаржать минимум 8 символов, одно число, одну заглавную букву.") //https://stackoverflow.com/questions/19605150
    String password;
}
