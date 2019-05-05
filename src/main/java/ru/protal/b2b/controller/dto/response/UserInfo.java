package ru.protal.b2b.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    Long id;
    String login;
    String firstName;
    String middleName;
    String lastName;
    String email;
    String status;
}
