package ru.protal.b2b.service.messaging.kafkastub;

import java.util.concurrent.TimeoutException;

public interface KafkaTemplate<T,R> {

    void send(T t, R r) throws TimeoutException;
}
