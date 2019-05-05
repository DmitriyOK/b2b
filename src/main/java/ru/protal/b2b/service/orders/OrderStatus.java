package ru.protal.b2b.service.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  OrderStatus {
    NEW(1L), DONE(2L), IN_PROGRESS(3L);

    private Long id;
}
