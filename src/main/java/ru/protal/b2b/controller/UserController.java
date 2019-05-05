package ru.protal.b2b.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.controller.dto.response.UserRegistrationResponse;
import ru.protal.b2b.service.persistance.UserService;

@RestController
@RequestMapping(path = "openapi/v1/")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/registration")
    UserRegistrationResponse registrationUser(@RequestBody UserRegistrationInfo user){
        UserInfo userInfo = userService.saveOne(user);
        return UserRegistrationResponse.builder()
                .userId(userInfo.getId())
                .status(userInfo.getStatus())
                .build();
    }
}
