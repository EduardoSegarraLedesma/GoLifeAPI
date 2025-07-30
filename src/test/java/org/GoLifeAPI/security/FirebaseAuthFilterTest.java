package org.GoLifeAPI.security;

import org.GoLifeAPI.handler.GlobalExceptionHandler;
import org.GoLifeAPI.infrastructure.FirebaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebaseAuthFilterTest {

    private MockMvc mockMvc;

    @Mock
    FirebaseService firebaseService;

    @InjectMocks
    FirebaseAuthFilter firebaseAuthFilter;
    @InjectMocks
    GlobalExceptionHandler exceptionHandler;

    @RestController
    static class DummyController {
        @GetMapping("/secure")
        public String secure() {
            return "OK";
        }
    }

    @BeforeAll
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(firebaseService.verifyBearerToken("Bearer good.token")).willReturn("test-uid");
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(exceptionHandler)
                .addFilter(firebaseAuthFilter, "/*")
                .build();
    }

    @Test
    void whenValidToken_200() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearer good.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void whenInvalidToken_401() throws Exception {
        given(firebaseService.verifyBearerToken("Bearer bad.token")).willReturn(null);

        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearer bad.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token inválido"));
    }

    @Test
    void whenMissingHeader_401() throws Exception {
        mockMvc.perform(get("/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falta el encabezado Authorization"));
        verify(firebaseService, never()).verifyBearerToken(anyString());
    }

    @Test
    void whenEmptyHeader_401() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falta el encabezado Authorization"));

    }

    @Test
    void whenNonBearerScheme_401() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falta el encabezado Authorization"));
        verify(firebaseService, never()).verifyBearerToken(any());
    }

    @Test
    void whenLowerCaseBearer_401() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", "bearer good.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falta el encabezado Authorization"));
        verify(firebaseService, never()).verifyBearerToken(anyString());
    }

    @Test
    void whenNoSpaceAfterBearer_401() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearergood.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Falta el encabezado Authorization"));
        verify(firebaseService, never()).verifyBearerToken(anyString());
    }
}