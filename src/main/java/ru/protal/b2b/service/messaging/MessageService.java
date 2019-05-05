package ru.protal.b2b.service.messaging;

import ru.protal.b2b.service.messaging.messages.in.ServiceInMessage;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;

import java.util.List;

public interface MessageService {

    void send(ServiceOutMessage serviceMessage) throws Exception;

    void listen(String message) throws Exception;

    boolean isAlive();

    List<ServiceInMessage> poll(String fromId);
}
