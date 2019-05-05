package ru.protal.b2b.service.orders.registrationuser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.repository.OrderRepository;
import ru.protal.b2b.repository.UserRepository;
import ru.protal.b2b.repository.dao.OrderDao;
import ru.protal.b2b.repository.dao.OrderTransitionDao;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.service.messaging.KafkaMessageService;
import ru.protal.b2b.service.messaging.messages.MessageFactory;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;
import ru.protal.b2b.service.orders.OrderStateResult;
import ru.protal.b2b.service.persistance.UserStatus;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static ru.protal.b2b.repository.dao.UserDao.from;
import static ru.protal.b2b.service.orders.OrderAction.REGISTRATION;
import static ru.protal.b2b.service.orders.OrderEntity.USER;
import static ru.protal.b2b.service.orders.OrderState.*;
import static ru.protal.b2b.service.orders.OrderStateResult.ERROR;
import static ru.protal.b2b.service.orders.OrderStateResult.SUCCESS;
import static ru.protal.b2b.service.orders.OrderStatus.IN_PROGRESS;
import static ru.protal.b2b.service.orders.OrderStatus.NEW;
import static ru.protal.b2b.service.persistance.UserStatus.ACTIVATION;

@Service
public class EmbeddedRegistrationManager implements OrderManager {

    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private KafkaMessageService messageService;
    private MessageFactory factory;
    private ConcurrentHashMap<Long, OrderDao> currentOrders;
    private ConcurrentLinkedQueue<OrderDao> futureOrders;
    private ExecutorService taskPool = Executors.newFixedThreadPool(getRuntime().availableProcessors());

    @Autowired
    public EmbeddedRegistrationManager(UserRepository userRepository, OrderRepository orderRepository,
                                       @Lazy KafkaMessageService messageService, MessageFactory factory) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.messageService = messageService;
        this.factory = factory;
    }

    @PreDestroy
    void shutdown(){
        try {
            taskPool.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //init all unfinished orders
        currentOrders = new ConcurrentHashMap<>();
        List<OrderDao> orders = orderRepository.findAllByStatusId(IN_PROGRESS.getId());
        orders.forEach(ord -> currentOrders.put(ord.getOrderId(), ord));
        //create orders for NEW users
        futureOrders = new ConcurrentLinkedQueue<>();
        List<UserDao> allByQuery = userRepository.findAllByQuery();
        allByQuery.forEach(user -> putTask(from(user)));
    }

    @Override
    public void handleMessage(VerifyInMessage inMessage, UserInfo userInfo) { // как вариант использовать apache akka и под каждый
        callbackProcessStep(inMessage, userInfo);                           // Step использовать актор для производительности
        sendEmailNotify(inMessage, userInfo);
        finishOrderStep(userInfo.getId());
        currentOrders.remove(userInfo.getId());
    }

    public void putTask(UserInfo userInfo) {
        OrderTransitionDao transition = OrderTransitionDao.builder()
                .fromStateId(START.getId())
                .toStateId(VERIFY.getId())
                .resultId(OrderStateResult.START.getId())
                .build();

        OrderDao newOrder = OrderDao.builder()
                .actionId(REGISTRATION.getId())
                .entityId(USER.getId())
                .statusId(NEW.getId())
                .userId(userInfo.getId())
                .transitions(Arrays.asList(transition))
                .build();
        futureOrders.add(newOrder);

        taskPool.submit(() -> startOrderAndUpdateUserStatus(userInfo)); //Как разрешить конфликт очередности
        // запуска новых и не завершенных
    }

    @Override
    public void reloadOrder(Long orderId) { //use for support by user request.
        Optional<OrderDao> order = orderRepository.findById(orderId);
        order.ifPresent(orderDao -> futureOrders.add(orderDao));
    }

    void startOrderAndUpdateUserStatus(UserInfo userInfo) {
        OrderDao order = futureOrders.poll();
        currentOrders.put(order.getUserId(), order);
        messageService.send(factory.getVerifyMessage(userInfo));
        UserDao userDao = userRepository.findById(userInfo.getId()).get();
        userDao.setStatus(UserStatusDao.builder().statusId(ACTIVATION.getId()).build());
        userRepository.save(userDao);
    }

    @Transactional
    void updateUserOrder(OrderDao order, UserStatusDao userStatus) {
        UserDao user = userRepository.findById(order.getUserId()).get();
        user.setStatus(userStatus);
        userRepository.save(user);
        orderRepository.save(order);
    }

    private void sendEmailNotify(VerifyInMessage inMessage, UserInfo userInfo) {
        //Step email notify
        ServiceOutMessage message = factory.getEmailNotify(userInfo, inMessage);
        messageService.send(message);
    }

    private void callbackProcessStep(VerifyInMessage inMessage, UserInfo userInfo) {
        //Step verify callback process
        OrderDao order = currentOrders.get(userInfo.getId());
        OrderTransitionDao transition = order.getTransitions().get(0);

        if (Objects.equals(inMessage.getStatus(), "ok")) {
            transition.setResultId(SUCCESS.getId());
        } else {
            transition.setResultId(ERROR.getId());
        }
        OrderTransitionDao newTransition = OrderTransitionDao.builder()
                .order(order)
                .fromStateId(VERIFY.getId())
                .toStateId(EMAIL_NOTIFY.getId())
                .resultId(OrderStateResult.START.getId())
                .build();
        order.getTransitions().add(newTransition);

        UserStatusDao userStatus = UserStatusDao.builder().statusId(UserStatus.ERROR.getId()).build();
        updateUserOrder(order, userStatus);
    }

    private void finishOrderStep(Long userId){
        OrderDao order = currentOrders.get(userId);
        //Step finish order
        OrderTransitionDao lastTransition = order.getTransitions().get(0);
        lastTransition.setResultId(SUCCESS.getId());
        OrderTransitionDao finalTransition = OrderTransitionDao.builder()
                .order(order)
                .fromStateId(EMAIL_NOTIFY.getId())
                .toStateId(DONE.getId())
                .resultId(SUCCESS.getId())
                .build();

        order.getTransitions().add(finalTransition);
        orderRepository.save(order);
    }
}
