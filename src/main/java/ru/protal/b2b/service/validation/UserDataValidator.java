package ru.protal.b2b.service.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.exceptions.UserValidationException;
import ru.protal.b2b.repository.dao.UserDao;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class UserDataValidator{

    private Validator validator;

    @Autowired
    public UserDataValidator(Validator validator) {
        this.validator = validator;
    }

    public void validateUserData(UserRegistrationInfo user){
        Set<ConstraintViolation<UserRegistrationInfo>> result = validator.validate(user);

        if (!result.isEmpty()){
            Map conflicts = result.stream().collect(
                    groupingBy(ConstraintViolation::getPropertyPath, mapping(ConstraintViolation::getMessageTemplate, toList())));
            throw new UserValidationException(conflicts);
        }
    }

    public void checkIfExist(UserRegistrationInfo userRegInfo, UserDao userDao){
        Map<String, String> conflicts = new HashMap<String, String>();

        if (userRegInfo.getEmail().equals(userDao.getEmail())){
            conflicts.put("email", "email уже существует");
        }
        if (userRegInfo.getLogin().equals(userDao.getLogin())){
            conflicts.put("login", "login уже существует");
        }

        if(!conflicts.isEmpty()){
            throw new UserValidationException(conflicts);
        }
    }
}
