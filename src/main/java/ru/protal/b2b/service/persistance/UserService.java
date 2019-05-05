package ru.protal.b2b.service.persistance;

import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;

public interface UserService {

    UserInfo saveOne(UserRegistrationInfo user);

    UserInfo findByLogin(String login);
}
