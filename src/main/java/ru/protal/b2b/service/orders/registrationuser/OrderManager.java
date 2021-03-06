package ru.protal.b2b.service.orders.registrationuser;

import org.springframework.beans.factory.InitializingBean;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;

import java.util.concurrent.atomic.AtomicBoolean;

public interface OrderManager extends InitializingBean {

    void putNewTask(UserInfo userInfo);

    void reloadOrder(Long orderId);

    void handleMessage(VerifyInMessage inMessage);

    boolean isAlive();
}
