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
import ru.protal.b2b.service.orders.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.protal.b2b.service.orders.OrderAction.REGISTRATION;
import static ru.protal.b2b.service.orders.OrderEntity.USER;
import static ru.protal.b2b.service.orders.OrderState.EMAIL_NOTIFY;
import static ru.protal.b2b.service.orders.OrderState.START;
import static ru.protal.b2b.service.orders.OrderState.VERIFY;
import static ru.protal.b2b.service.orders.OrderStatus.DONE;
import static ru.protal.b2b.service.orders.OrderStatus.NEW;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void testPersistOrder(){

        OrderTransitionDao orderTransition = OrderTransitionDao.builder()
                .fromStateId(START.getId())
                .toStateId(OrderState.VERIFY.getId())
                .resultId(OrderStateResult.ERROR.getId())
                .build();


        OrderDao order = OrderDao.builder()
                .actionId(REGISTRATION.getId())
                .entityId(USER.getId())
                .statusId(DONE.getId())
                .transitions(Arrays.asList(orderTransition))
                .build();

        UserDao user = UserDao.builder()
                .firstName("petya").middleName("ivanovich").lastName("petrov")
                .email("pet@das.com").login("pet234").password("assd")
                .status(UserStatusDao.builder()
                        .status("NEW").build())
                .build();

        user.setOrder(order);
        order.setUser(user);
        orderTransition.setOrder(order);

        entityManager.persist(order);
        entityManager.flush();

        OrderDao orderDao = orderRepository.findAll().get(0);
        assertThat(orderDao.getTransitions().get(0).getResultId() == 2L);
        assertThat(orderDao.getTransitions().get(0).getOrder().getOrderId()).isPositive();
    }


    @Test
    public void testSortTransition(){

        UserDao user = UserDao.builder()
                .firstName("petya").middleName("ivanovich").lastName("petrov")
                .email("pet@das.com").login("pet234").password("assd")
                .status(UserStatusDao.builder()
                        .status("NEW").build())
                .build();

        OrderTransitionDao transition = OrderTransitionDao.builder()
                .fromStateId(START.getId())
                .toStateId(VERIFY.getId())
                .resultId(OrderStateResult.START.getId())
                .build();

        OrderDao newOrder = OrderDao.builder()
                .actionId(REGISTRATION.getId())
                .entityId(USER.getId())
                .statusId(NEW.getId())
                .transitions(Arrays.asList(transition))
                .build();

        user.setOrder(newOrder);
        newOrder.setUser(user);
        transition.setOrder(newOrder);

        newOrder = entityManager.persist(newOrder);
        entityManager.flush();

        OrderTransitionDao oldTransit = newOrder.getTransitions().get(0);
        oldTransit.setResultId(OrderStateResult.SUCCESS.getId());

        OrderTransitionDao newTransit = OrderTransitionDao.builder()
                .fromStateId(VERIFY.getId())
                .toStateId(EMAIL_NOTIFY.getId())
                .resultId(OrderStateResult.START.getId())
                .build();
        newTransit.setOrder(newOrder);
        newOrder.setTransitions(Arrays.asList(oldTransit, newTransit));

        entityManager.persist(newOrder);
        entityManager.flush();
        Collections.sort(newOrder.getTransitions());

        assertThat(newOrder.getTransitions().size()).isEqualTo(2);
        assertThat(newOrder.getTransitions().get(0).getToStateId()).isEqualTo(EMAIL_NOTIFY.getId());
    }
}
