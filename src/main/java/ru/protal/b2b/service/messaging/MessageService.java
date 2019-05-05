package ru.protal.b2b.service.messaging;

import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;

public interface MessageService {

    void send(ServiceOutMessage serviceMessage) throws Exception;

    void listen(String message) throws Exception;
}
