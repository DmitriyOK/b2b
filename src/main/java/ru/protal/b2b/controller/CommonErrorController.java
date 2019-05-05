package ru.protal.b2b.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import ru.protal.b2b.controller.dto.response.ErrorDescription;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class CommonErrorController implements ErrorController {

    private ErrorAttributes errorAttributes;

    private static final String ERROR_PATH ="/error";

    @Autowired
    public CommonErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @GetMapping(path = ERROR_PATH)
    private ErrorDescription getMessage(HttpServletRequest request, HttpServletResponse response){
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(servletWebRequest, true);
    return ErrorDescription.builder()
            .code(Integer.valueOf(attributes.get("status").toString()))
            .userMessage(attributes.get("message").toString())
            .developerMessage(attributes.get("error").toString())
            .path(attributes.get("path").toString())
            .build();

    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
