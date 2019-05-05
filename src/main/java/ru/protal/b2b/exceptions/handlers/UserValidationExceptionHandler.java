package ru.protal.b2b.exceptions.handlers;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.protal.b2b.controller.dto.response.ErrorDescription;
import ru.protal.b2b.exceptions.UserValidationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Order(value = Ordered.LOWEST_PRECEDENCE - 1)
@ControllerAdvice
public class UserValidationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = UserValidationException.class)
    ResponseEntity<ErrorDescription> handleUserValidationException(UserValidationException e, HttpServletRequest request) {
        ErrorDescription result = ErrorDescription.builder()
                .code(BAD_REQUEST.value())
                .userMessage("Не корректные данные регистрации")
                .developerMessage(e.toString())
                .path(request.getRequestURI())
                .conflicts(e.getConflicts())
                .build();

        return new ResponseEntity<>(result, BAD_REQUEST);
    }

}
