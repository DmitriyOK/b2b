package ru.protal.b2b.service.orders.registrationuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.protal.b2b.service.messaging.KafkaMessageServiceStub;
import ru.protal.b2b.service.messaging.messages.MessageFactory;
import ru.protal.b2b.service.messaging.messages.in.ServiceInMessage;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final static Logger log = LoggerFactory.getLogger(EmbeddedRegistrationManager.class);
    private AtomicBoolean isAlive = new AtomicBoolean(true);
    private AtomicInteger waitCallCount = new AtomicInteger(0);
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private KafkaMessageServiceStub messageService;
    private MessageFactory factory;
    private ConcurrentHashMap<Long, OrderDao> currentOrders;
    private ConcurrentLinkedQueue<OrderDao> futureOrders;
    private ExecutorService taskPool = Executors.newFixedThreadPool(getRuntime().availableProcessors());

    @Autowired
    public EmbeddedRegistrationManager(UserRepository userRepository, OrderRepository orderRepository,
                                       @Lazy KafkaMessageServiceStub messageService, MessageFactory factory) {
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
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        restoreTasks();
    }

    public void putNewTask(UserInfo userInfo) {
        OrderTransitionDao transition = OrderTransitionDao.builder()
                .fromStateId(START.getId())
                .toStateId(VERIFY.getId())
                .resultId(OrderStateResult.START.getId())
                .build();

        OrderDao newOrder = OrderDao.builder()
                .actionId(REGISTRATION.getId())
                .entityId(USER.getId())
                .statusId(NEW.getId())
                .user(userRepository.getOne(userInfo.getId()))
                .transitions(Arrays.asList(transition))
                .build();
        futureOrders.add(newOrder);
        addTaskToPool(userInfo);
    }

    private void addTaskToPool(UserInfo userInfo) {
        taskPool.submit(() -> {
            try {
                startRegistration(userInfo);
            } catch (TimeoutException e) {
                log.error(e.getMessage(), e);
                waitingForConnect();
            }
        });
    }

    @Override// как вариант использовать apache akka и под каждый Step использовать актор для повышения производительности
    public void handleMessage(VerifyInMessage inMessage) {
        try {
            UserDao userDao = userRepository.findById(inMessage.getUserId()).get();
            UserInfo userInfo = from(userDao);
            callbackProcessStep(inMessage, userInfo);
            sendEmailNotify(inMessage, userInfo);
            finishOrderStep(userInfo.getId());
        } catch (TimeoutException e) {
            log.error(e.getMessage(), e);
            waitingForConnect();
        }
    }

    @Override
    public void reloadOrder(Long orderId) { //use for support by user request. TODO implement controller
        Optional<OrderDao> order = orderRepository.findById(orderId);
        order.ifPresent(orderDao -> futureOrders.add(orderDao));
    }

    void startRegistration(UserInfo userInfo) throws TimeoutException {
        OrderDao order = futureOrders.poll();
        currentOrders.put(order.getUser().getUserId(), order);
        messageService.send(factory.getVerifyMessage(userInfo));
        UserDao userDao = userRepository.findById(userInfo.getId()).get();
        userDao.setStatus(UserStatusDao.builder().statusId(ACTIVATION.getId()).build());
        userRepository.save(userDao);
    }

    @Transactional
    void updateUserOrder(OrderDao order, UserStatusDao userStatus) {
        UserDao user = userRepository.findById(order.getUser().getUserId()).get();
        user.setStatus(userStatus);
        userRepository.save(user);
        orderRepository.save(order);
    }

    private void sendEmailNotify(VerifyInMessage inMessage, UserInfo userInfo) throws TimeoutException {
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
        currentOrders.remove(userId);
    }

    public boolean isAlive() {
        return isAlive.get();
    }

    private void waitingForConnect() {  /*это следует выполнить в одном потоке. Т.к. потоки, которые поймали эксепшн,
                                         вызовут этот метод несколько раз и после восстановления соединения
                                         они восстановят контекст так же несколько раз :)
                                         И нужно чтобы этот единственный поток был живой.
                                         С другой стороны можно реализовать мнопоточное восстановление....
                                         */
        if(isAlive.get()){
            isAlive.set(false);
        }
        if(waitCallCount.getAndIncrement() > 0){
            return;
        }

        //До этого места могут дойти два потока?
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(() -> {
            try {
                if (messageService.isAlive()) {
                    restoreTasks();
                    waitCallCount.decrementAndGet();
                    isAlive.set(true);
                    service.shutdown();
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }, 1500, 1500, TimeUnit.MILLISECONDS);
    }

    private void restoreTasks() {
        /*//TODO покрыть тестами и потестить
            1. Не взлетели на старте -> просто сказали в лог и ждем.
            2. Упала очередь в рантайме -> 2.1. Остановка обработки заказов (shutdown pool)
                                                (Потеря текущих состояний не сильно страшна, т.к всё сохраняется.)
                                                Мы знаем у какого пользователя и на каком этапе был каждый заказ,
                                                нужно просто их найти и переключиться на последний стейт.
                                                OrderTransitionDao имплементит Сomparable по transitionId и самый последний выполняемый
                                                стейт всегда будет самым первым.
                                                За повторную отправку сообщений можно не переживать. Это мало вероятно т.к.
                                                стейты обновляются после отправки собщения и их статус START означает,
                                                 что он либо выполнялся, либо начал выполнятся. Если всё же это случится
                                                 то Kafka решит эту проблему т.к. поддерживает идемпотентность из коробки.

                                           2.2 Дождаться восстановления.
                                           2.3 Восстановить и завершить IN_PROGRESS заказы
                                           2.4 Найти пользователей для которых заказы не созданы и создать их.
                                           2.5 Выжить под всплеском нагрузки...

          3. Упали в рантайме сами. -> Повторяем дейсвия с п. 2.3 //TODO как об этом узнает поток?
         */
        //init all unfinished orders
        currentOrders = new ConcurrentHashMap<>();
        List<OrderDao> orders = orderRepository.findAllByStatusId(IN_PROGRESS.getId());
        orders.forEach(ord -> {
                    UserInfo userInfo = from(userRepository.findById(ord.getUser().getUserId()).get());
                    OrderTransitionDao transition = ord.getTransitions().get(0);
                    if(Objects.equals(VERIFY.getId(), transition.getToStateId())){
                       futureOrders.add(ord);
                       addTaskToPool(userInfo);
                    }
                    if(Objects.equals(EMAIL_NOTIFY.getId(), transition.getToStateId())){
                        currentOrders.put(ord.getOrderId(), ord);
                        taskPool.submit(() -> {
                            List<ServiceInMessage> message = messageService.poll("correlationId");//TODO add to state context
                            VerifyInMessage verifyMsg = (VerifyInMessage) message.get(0).getMessage();
                            try {
                                sendEmailNotify(verifyMsg, userInfo);
                            }catch (TimeoutException e){
                                log.error(e.getMessage(), e);
                                waitingForConnect();
                            }

                        });
                    }
                }
            );
        //create orders for NEW users
        futureOrders = new ConcurrentLinkedQueue<>();
        List<UserDao> allByQuery = userRepository.findAllWhereOrderIsNull();
        allByQuery.forEach(user -> putNewTask(from(user)));
    }
}
