package ru.protal.b2b.service.persistance;

import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.repository.dao.UserDao;

import java.util.List;

public interface UserService {

    UserInfo saveOne(UserRegistrationInfo user);

    UserInfo findByLogin(String login);
}
