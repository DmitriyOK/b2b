package ru.protal.b2b.service.orders;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStateResult {
    START(1L), SUCCESS(2L), ERROR(3L), FAIL(4L);

    private Long id;
}
