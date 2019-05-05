package ru.protal.b2b;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.protal.b2b.configuration.security.SecurityConfiguration;
import ru.protal.b2b.controller.UserController;
import ru.protal.b2b.controller.dto.request.UserRegistrationInfo;
import ru.protal.b2b.controller.dto.response.UserInfo;
import ru.protal.b2b.service.persistance.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    UserService userService;

    @Test
    public void registrationRequestAcceptSuccess() throws Exception {

        //given
        ObjectMapper mapper = new ObjectMapper();
        UserRegistrationInfo regInfo = new UserRegistrationInfo();
        regInfo.setLogin("login");
        String regInfoAsJson = mapper.writeValueAsString(regInfo);
        //and
        UserInfo mockUserInfo = UserInfo.builder().id(1L).status("NEW").build();
        when(userService.saveOne(any(UserRegistrationInfo.class))).thenReturn(mockUserInfo);
        //when
        mvc.perform(
                post("/openapi/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(regInfoAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("NEW"));
    }
}
