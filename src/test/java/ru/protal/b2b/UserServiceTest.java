package ru.protal.b2b;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.protal.b2b.exceptions.UserValidationException;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.repository.UserRepository;
import ru.protal.b2b.service.orders.registrationuser.OrderManager;
import ru.protal.b2b.service.persistance.UserService;
import ru.protal.b2b.service.validation.UserDataValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.protal.b2b.service.persistance.UserStatus.ACTIVE;
import static ru.protal.b2b.service.persistance.UserStatus.NEW;


@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    UserDataValidator validator;

    @MockBean
    OrderManager orderManager;

    @Captor
    private ArgumentCaptor<UserDao> userDaoArg;

    @Test
    public void saveUserSuccess(){
        //given
        UserRegistrationInfo userRegInfo = UserRegistrationInfo.builder()
                .login("ivan234").password("qwerT1234").firstName("ivan")
                .middleName("ivanovich").lastName("ivanov").email("d@d.com")
                .build();

        UserDao daoOutUser = UserDao.builder()
                .userId(1L)
                .login("ivan234").password("dasdas#$%^12312das").email("d@d.com")
                .firstName("ivan").middleName("ivanovich").lastName("ivanov")
                .status(UserStatusDao.builder()
                        .status(NEW.toString()).statusId(1L).build())
                .build();

        UserInfo expectedUserInfo = UserInfo.builder()
                .id(1L).login("ivan234").email("d@d.com")
                .firstName("ivan").middleName("ivanovich")
                .lastName("ivanov").status(NEW.toString())
                .build();
        //and
        when(userRepository.save(any(UserDao.class))).thenReturn(daoOutUser);
        doNothing().when(validator).validateUserData(any(UserRegistrationInfo.class));
        //when
        UserInfo currentUserInfo = userService.saveOne(userRegInfo);
        //then
        verify(userRepository).save(userDaoArg.capture());
        assertThat(expectedUserInfo).isEqualTo(currentUserInfo);
        assertThat(userDaoArg.getValue().getPassword()).isNotEqualTo("qwerT1234"); //check encode password
    }

    @Test(expected = UserValidationException.class)
    public void duplicateRegistrationSuccess(){
        //given
        UserRegistrationInfo userRegInfo = UserRegistrationInfo.builder()
                .login("ivan234").email("d@d.com")
                .build();
        //and
        UserDao daoOutUser = UserDao.builder()
                .userId(1L)
                .login("ivan234").password("dasdas#$%^12312das").email("d@d.com")
                .firstName("ivan").middleName("ivanovich").lastName("ivanov")
                .status(UserStatusDao.builder()
                        .status(NEW.toString()).statusId(1L).build())
                .build();

        when(userRepository.findByLoginOrEmail("ivan234", "d@d.com")).thenReturn(daoOutUser);
        doThrow(UserValidationException.class)
                .when(validator).checkIfExist(any(UserRegistrationInfo.class), any(UserDao.class));
        //when
        userService.saveOne(userRegInfo);
    }

    @Test
    public void findByLoginSuccess(){
        //given
        UserDao mockUser = UserDao.builder()
                .userId(1L)
                .login("ivan234")
                .password("pasaas2372iu3128@^#%(!!asDsdc")
                .email("d@d.com")
                .firstName("ivan")
                .middleName("ivanovich")
                .lastName("ivanov")
                .status(UserStatusDao.builder()
                        .status(ACTIVE.toString())
                        .statusId(1L).build())
                .build();

        UserInfo expectedUser = UserInfo.builder()
                .id(1L)
                .login("ivan234")
                .email("d@d.com")
                .firstName("ivan")
                .middleName("ivanovich")
                .lastName("ivanov")
                .status(ACTIVE.toString())
                .build();
        //and
        when(userRepository.findByLogin("ivan234")).thenReturn(mockUser);
        //when
        UserInfo currentUser = userService.findByLogin("ivan234");
        //then
        assertThat(expectedUser).isEqualTo(currentUser);
    }


    @Test
    public void putTaskSkipWhenOrderManagerIsNotAlive(){
        //given
        mocksForAliveCheck(false);
        //then
        verify(orderManager,never()).putTask(any(UserInfo.class));
    }

    @Test
    public void putTaskSkipWhenOrderManagerIsAlive(){
        //given
        mocksForAliveCheck(true);
        //then
        verify(orderManager, times(1)).putTask(any(UserInfo.class));
    }

    void mocksForAliveCheck(boolean isAlive){
        //given
        UserDao daoOutUser = UserDao.builder()
                .userId(1L)
                .login("ivan234").password("dasdas#$%^12312das").email("d@d.com")
                .firstName("ivan").middleName("ivanovich").lastName("ivanov")
                .status(UserStatusDao.builder()
                        .status(NEW.toString()).statusId(1L).build())
                .build();

        UserRegistrationInfo userRegInfo = UserRegistrationInfo.builder()
                .login("ivan234").email("d@d.com").password("help_me_plz:)").build();
        //and
        when(userRepository.findByLogin(anyString())).thenReturn(null);
        when(userRepository.save(any(UserDao.class))).thenReturn(daoOutUser);
        doNothing().when(validator).validateUserData(any(UserRegistrationInfo.class));
        when(orderManager.isAlive()).thenReturn(isAlive);
        userService.saveOne(userRegInfo);
    }
}
