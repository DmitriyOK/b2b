package ru.protal.b2b.service.messaging.messages;

import org.springframework.stereotype.Component;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.messaging.messages.out.EmailNotifyOutMessage;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;
import ru.protal.b2b.service.messaging.messages.out.VerifyOutMessage;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.UUID;

@Component
public class MessageFactoryImpl implements MessageFactory {

    private HashMap<String, String> zooService = new HashMap<>();
    private static final String MAILER_KEY = "mailerQueue";
    private static final String VERIFY_KEY = "verifyQueue";
    private static final String VERIFY_CALLBACK_KEY = "verifyCallback";
    private static final String SERVICE_NAME = "REGISTRATOR";
    @PostConstruct
    void initParams(){
        zooService.put("mailerQueue","localhost:mailer");
        zooService.put("verifyQueue","someservice:verify");
        zooService.put("verifyCallback","localhost:verifyCallback");
    }

    @Override
    public ServiceOutMessage getVerifyMessage(UserInfo userInfo) {

            return ServiceOutMessage.<VerifyOutMessage>builder()
                    .correlationId(UUID.randomUUID().toString())
                    .queue(zooService.get(VERIFY_KEY))
                    .replyTo(zooService.get(VERIFY_CALLBACK_KEY))
                    .serviceName(SERVICE_NAME)
                    .message(VerifyOutMessage.builder()
                            .firstName(userInfo.getFirstName())
                            .middleName(userInfo.getMiddleName())
                            .lastName(userInfo.getLastName())
                            .userId(userInfo.getId())
                            .build())
                    .build();
        }

    @Override
    public ServiceOutMessage getEmailNotify(UserInfo userInfo, VerifyInMessage verifyOutMessage) {
         return ServiceOutMessage.<EmailNotifyOutMessage>builder()
                .correlationId(UUID.randomUUID().toString())
                .queue(zooService.get(MAILER_KEY))
                .serviceName(SERVICE_NAME)
                .message(EmailNotifyOutMessage.builder()
                        .userEmail(userInfo.getEmail())
                        .message(verifyOutMessage.getDescription())
                        .build())
                .build();
    }
}

