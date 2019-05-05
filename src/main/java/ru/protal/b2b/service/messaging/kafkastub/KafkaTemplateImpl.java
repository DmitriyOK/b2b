package ru.protal.b2b.service.messaging.kafkastub;

import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;


@Component
public class KafkaTemplateImpl<T,R> implements KafkaTemplate<T, R> {

    @Override
    public void send(T t, R r) throws TimeoutException {

    }
}
