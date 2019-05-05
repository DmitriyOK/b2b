package ru.protal.b2b.exceptions.handlers;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.protal.b2b.controller.dto.response.ErrorDescription;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Order
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ErrorDescription> handleRuntimeException(Exception e, HttpServletRequest request) {
        ErrorDescription result = ErrorDescription.builder()
                .code(INTERNAL_SERVER_ERROR.value())
                .userMessage(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .developerMessage(e.toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(result, INTERNAL_SERVER_ERROR);
    }
}
