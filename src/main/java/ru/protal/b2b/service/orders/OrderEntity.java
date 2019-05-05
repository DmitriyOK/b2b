package ru.protal.b2b.service.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  OrderEntity {
    USER(1L);

    private Long id;
}
