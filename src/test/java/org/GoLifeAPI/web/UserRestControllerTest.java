package org.GoLifeAPI.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.handler.GlobalExceptionHandler;
import org.GoLIfeAPI.bussiness.implementation.UserService;
import org.GoLIfeAPI.web.UserRestController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Principal testPrincipal;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestController userRestController;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeAll
    void beforeAll() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userRestController)
                .setControllerAdvice(exceptionHandler)
                .build();

        // simulate an authenticated user with UID = "test-uid"
        testPrincipal = new UsernamePasswordAuthenticationToken("test-uid", null);
    }


}