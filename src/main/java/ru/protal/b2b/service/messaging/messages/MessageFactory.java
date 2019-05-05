package ru.protal.b2b.service.messaging.messages;

import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.service.messaging.messages.in.VerifyInMessage;
import ru.protal.b2b.service.messaging.messages.out.ServiceOutMessage;

public interface MessageFactory {

    ServiceOutMessage getVerifyMessage(UserInfo userInfo);
    ServiceOutMessage getEmailNotify(UserInfo userInfo, VerifyInMessage verifyInMessage);
}
