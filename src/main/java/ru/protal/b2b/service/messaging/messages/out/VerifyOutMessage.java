package ru.protal.b2b.service.messaging.messages.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOutMessage {
    Long userId;
    String firstName;
    String middleName;
    String lastName;
}
