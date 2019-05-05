package ru.protal.b2b;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import ru.protal.b2b.repository.OrderRepository;
import ru.protal.b2b.repository.dao.OrderDao;
import ru.protal.b2b.repository.dao.OrderTransitionDao;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.repository.UserRepository;
import ru.protal.b2b.service.orders.OrderState;
import ru.protal.b2b.service.orders.OrderStateResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.protal.b2b.service.orders.OrderAction.REGISTRATION;
import static ru.protal.b2b.service.orders.OrderEntity.USER;
import static ru.protal.b2b.service.orders.OrderState.START;
import static ru.protal.b2b.service.orders.OrderStatus.DONE;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void saveUserSuccess(){
        UserDao expectedUser = UserDao.builder()
                .firstName("ivan")
                .middleName("ivanovich")
                .lastName("ivanov")
                .email("abcs@das.com")
                .login("ivan234")
                .password("asasd")
                .status(UserStatusDao.builder()
                        .status("NEW").build())
                .build();

        entityManager.persist(expectedUser);
        entityManager.flush();

        UserDao currentUser = userRepository.findByLogin("ivan234");

        assertThat(expectedUser).isEqualTo(currentUser);
    }


    @Test
    public void findUserWithoutOrdersSuccess(){

        //given
        UserDao userWithoutOrder = UserDao.builder()
                .firstName("petya").middleName("ivanovich").lastName("petrov")
                .email("pet@das.com").login("pet234").password("assd")
                .status(UserStatusDao.builder()
                        .status("NEW").build())
                .build();


        UserDao userWithOrder = UserDao.builder() //TODO fix mapping on dao
                .firstName("ivan").middleName("ivanovich").lastName("ivanov")
                .email("abcs@das.com").login("ivan234").password("asasd")
                .status(UserStatusDao.builder()
                        .status("ACTIVATE").build())
                .build();

        OrderDao order = OrderDao.builder()
                .actionId(REGISTRATION.getId())
                .entityId(USER.getId()).statusId(DONE.getId())
                .build();

        OrderTransitionDao orderTransition = OrderTransitionDao.builder()
                .fromStateId(START.getId())
                .toStateId(OrderState.VERIFY.getId())
                .resultId(OrderStateResult.ERROR.getId())
                .build();

        userWithOrder.setOrder(order);
        order.setUser(userWithOrder);
        order.setTransitions(Arrays.asList(orderTransition));
        orderTransition.setOrder(order);
        //and
        userRepository.save(userWithoutOrder);
        userRepository.save(userWithOrder);
        orderRepository.save(order);
        //when
        List<UserDao> allByQuery = userRepository.findAllWhereOrderIsNull();
        //then
        assertThat(allByQuery.size() == 1).isTrue();
        assertThat(allByQuery.get(0).getLogin()).isEqualTo("pet234");
    }
}
