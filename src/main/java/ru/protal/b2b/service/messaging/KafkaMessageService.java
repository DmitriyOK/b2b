package ru.protal.b2b.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.protal.b2b.service.messaging.kafkastub.KafkaListener;
import ru.protal.b2b.service.messaging.kafkastub.KafkaTemplate;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.orders.registrationuser.OrderManager;


@Service
public class KafkaMessageService implements MessageService  {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper mapper;
    private OrderManager orderManager;

    public KafkaMessageService(KafkaTemplate<String, String> kafkaTemplate, OrderManager orderManager, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.orderManager = orderManager;
    }

    @Override
    @SneakyThrows
    public void send(ServiceOutMessage message){
        kafkaTemplate.send(message.getQueue(), mapper.writeValueAsString(message));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "verifyCallbackQueue") //TODO как заинжектить очередь динамически? на случай изменения?
    public void verifyListen(String message) {
        System.out.println("Verify callback: " + message);
        VerifyInMessage verifyInMessage = mapper.readValue(message, VerifyInMessage.class);
        orderManager.handleMessage(verifyInMessage, null);
    }
}
