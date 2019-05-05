package ru.protal.b2b.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.protal.b2b.service.messaging.kafkastub.KafkaListener;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.orders.registrationuser.OrderManager;


@Service
public class KafkaMessageService implements MessageService  {

    private ObjectMapper mapper;
    private OrderManager orderManager;

    public KafkaMessageService(OrderManager orderManager, ObjectMapper mapper) {
        this.mapper = mapper;
        this.orderManager = orderManager;
    }

    @Override
    @SneakyThrows
    public void send(ServiceOutMessage message){
        System.out.println("send to: "+message.getQueue());
        System.out.println("send body: "+mapper.writeValueAsString(message));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "verifyCallbackQueue") //TODO как заинжектить очередь динамически? на случай изменения?
    public void listen(String message) {
        System.out.println("Verify callback: " + message);
        VerifyInMessage verifyInMessage = mapper.readValue(message, VerifyInMessage.class);
        orderManager.handleMessage(verifyInMessage);
    }
}
