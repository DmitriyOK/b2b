package ru.protal.b2b.service.messaging.messages.out;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotifyOutMessage {
    String userEmail;
    String message;
}
