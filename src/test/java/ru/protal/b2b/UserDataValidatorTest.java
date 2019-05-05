package ru.protal.b2b;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.exceptions.UserValidationException;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.service.validation.UserDataValidator;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDataValidatorTest {

    @Autowired
    UserDataValidator validator;

    @Test
    public void checkIfAllFieldsIsNullSuccess() {
        UserRegistrationInfo userInfo = new UserRegistrationInfo();
        Set<String> expectedConflictsKeys = new HashSet<>();
        expectedConflictsKeys.addAll(Arrays.asList("login", "email", "password", "firstName", "lastName", "middleName"));
        Set<String> actualKeys = null;

        try {
            validator.validateUserData(userInfo);
        } catch (UserValidationException e) {
            actualKeys = convertToSet(e.getConflicts());
        }

        assertThat(expectedConflictsKeys).containsAll(actualKeys);
    }

    @Test
    public void checkIfFieldsContainsError() {
        UserRegistrationInfo userInfo = UserRegistrationInfo.builder()
                .email("sdakfj.com")
                .login("вф")
                .firstName("jasdgajsgdfhasdfhjasfdnjasfdjkagsdjvbashbcvhagsvchjgvjhgvh")
                .middleName("s")
                .lastName("ы")
                .password("")
                .build();
        Set<String> expectedConflictsKeys = new HashSet<>();
        expectedConflictsKeys.addAll(Arrays.asList("login", "email", "password", "firstName", "lastName", "middleName"));
        Set<String> actualKeys = null;

        try {
            validator.validateUserData(userInfo);
        } catch (UserValidationException e) {
            actualKeys = convertToSet(e.getConflicts());
        }

        assertThat(expectedConflictsKeys).containsAll(actualKeys);
    }

    @Test
    public void checkIfValidationSuccess() {
        UserRegistrationInfo userInfo = UserRegistrationInfo.builder()
                .email("sda@kfj.com")
                .login("nik2345")
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .password("cdx2Dcxz")
                .build();

        Set<String> actualKeys = null;

        try {
            validator.validateUserData(userInfo);
        } catch (UserValidationException e) {
            actualKeys = convertToSet(e.getConflicts());
        }

        assertThat(actualKeys).isNullOrEmpty();
    }

    @Test
    public void loginAlreadyExist(){
        //given
        Map conflicts = new HashMap<String, String>();
        UserDao daoOutUser = UserDao.builder()
                .userId(1L)
                .login("ivan234")
                .email("d@d.com")
                .build();
        //and
        UserRegistrationInfo userRegInfo = UserRegistrationInfo.builder()
                .login("ivan234")
                .email("d@d.com")
                .build();

        try {
            validator.checkIfExist(userRegInfo,daoOutUser);
        }catch (UserValidationException e){
            conflicts = e.getConflicts();
        }
        assertThat(conflicts.containsKey("login"));
        assertThat(conflicts.containsKey("email"));
    }

    @SuppressWarnings("unchecked")
    private Set<String> convertToSet(Map conflicts) {
        return (Set) conflicts.keySet()
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
