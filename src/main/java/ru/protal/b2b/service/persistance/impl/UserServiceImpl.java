package ru.protal.b2b.service.persistance.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.repository.UserRepository;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.service.orders.registrationuser.OrderManager;
import ru.protal.b2b.service.persistance.UserService;
import ru.protal.b2b.service.validation.UserDataValidator;

import static ru.protal.b2b.repository.dao.UserDao.from;
import static ru.protal.b2b.service.persistance.UserStatus.NEW;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserDataValidator validator;
    private OrderManager orderManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           UserDataValidator validator, OrderManager orderManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.orderManager = orderManager;
    }

    @Override
    public UserInfo saveOne(UserRegistrationInfo user) {

        UserDao byLoginOrEmail = userRepository.findByLoginOrEmail(user.getLogin(), user.getEmail());
        if(byLoginOrEmail != null){
            validator.checkIfExist(user, byLoginOrEmail);
        }
        validator.validateUserData(user);

        UserDao userDao = UserDao.builder()
                .login(user.getLogin())
                .password(encodePassword(user.getPassword()))
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .status(UserStatusDao.builder().statusId(NEW.getId()).build())
                .build();

        UserInfo userInfo = from(userRepository.save(userDao));
        if (orderManager.isAlive()){
                orderManager.putNewTask(userInfo);
        }
        return userInfo;
    }


    @Override
    public UserInfo findByLogin(String login) {
        UserDao userDao = userRepository.findByLogin(login);
        return from(userDao);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }


}
