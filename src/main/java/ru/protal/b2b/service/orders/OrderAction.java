package ru.protal.b2b.service.orders;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderAction {
    REGISTRATION(1L);

    private Long id;
}
