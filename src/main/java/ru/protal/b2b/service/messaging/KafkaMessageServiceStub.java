package ru.protal.b2b.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.protal.b2b.service.messaging.kafkastub.KafkaListener;
import ru.protal.b2b.service.messaging.messages.in.ServiceInMessage;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.messaging.messages.out.VerifyOutMessage;
import ru.protal.b2b.service.orders.registrationuser.OrderManager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;


@Service
public class KafkaMessageServiceStub implements MessageService  {

    private ObjectMapper mapper;
    private OrderManager orderManager;

    public KafkaMessageServiceStub(OrderManager orderManager, ObjectMapper mapper) {
        this.mapper = mapper;
        this.orderManager = orderManager;
    }

    @Override
    @SneakyThrows
    public void send(ServiceOutMessage message) throws TimeoutException {
        System.out.println("send to: "+message.getQueue());
        System.out.println("send body: "+mapper.writeValueAsString(message));

        if(System.currentTimeMillis() % 10 == 3) throw new TimeoutException();

        Thread.sleep(3000);
        if (message.getMessage() instanceof VerifyOutMessage){
            VerifyInMessage build = VerifyInMessage.builder()
                    .userId(((VerifyOutMessage) message.getMessage()).getUserId())
                    .description(new Random().nextInt() % 2 == 0 ? "ok" : "fail")
                    .build();
            listen(mapper.writeValueAsString(build));
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "verifyCallbackQueue") //TODO как заинжектить очередь динамически? на случай изменения?
    public void listen(String message) throws TimeoutException { //вообще это внешний метод какого producer-а.
                                                                // Надо почиать доку как обрабатывать ошибки
        System.out.println("Verify callback: " + message);
        VerifyInMessage verifyInMessage = mapper.readValue(message, VerifyInMessage.class);
        orderManager.handleMessage(verifyInMessage);
    }



    @Override
    public boolean isAlive() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<ServiceInMessage> poll(String fromId) {
        return Arrays.asList(ServiceInMessage.builder()
                .correlationId(fromId)
                .serviceName("VERIFY")
                .message(VerifyInMessage.builder()
                        .userId(12L)
                        .description("ok")
                        .status("ok")
                        .build())
                .build());
    }
}
