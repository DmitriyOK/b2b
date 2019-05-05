package ru.protal.b2b.service.persistance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

    NEW(1L), ACTIVE(2L), ACTIVATION(3L), ERROR(4L);

    private Long id;
}
