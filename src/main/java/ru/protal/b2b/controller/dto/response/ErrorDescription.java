package ru.protal.b2b.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDescription {

    private final String serviceName = "REGISTRATOR";

    Integer code;
    String userMessage;
    String developerMessage;
    String path;
    Object conflicts;
}
