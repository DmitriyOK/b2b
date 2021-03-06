package ru.protal.b2b.service.messaging.messages.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInMessage {
    String serviceName;
    String correlationId;
    Object message;
}
