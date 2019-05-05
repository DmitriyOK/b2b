package ru.protal.b2b.service.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderState {

    START(1L), DONE(2L), VERIFY(3L), EMAIL_NOTIFY(4L);

    private Long id;
}
