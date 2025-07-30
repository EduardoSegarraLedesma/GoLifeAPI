package org.GoLifeAPI.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.GoLifeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLifeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLifeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLifeAPI.dto.record.CreateNumRecordDTO;
import org.GoLifeAPI.security.RateLimitingFilter;
import org.GoLifeAPI.service.interfaces.IRecordService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecordRestController.class)
@AutoConfigureMockMvc
@Import(RecordRestControllerTest.TestSecurityConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecordRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IRecordService recordService;

    private UsernamePasswordAuthenticationToken defaultPrincipal;

    String uid;
    String mid;
    private String boolRecordJson;
    private String numRecordJson;
    private ResponseBoolGoalDTO responseBoolGoalDTO;
    private ResponseNumGoalDTO responseNumGoalDTO;

    @BeforeEach
    public void setUp() {
        uid = "test-uid";
        mid = "test-mid";
        defaultPrincipal = new UsernamePasswordAuthenticationToken(
                uid, null, Collections.emptyList()
        );
        clearInvocations(recordService);
        boolRecordJson = null;
        numRecordJson = null;
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Primary
        public RateLimitingFilter noOpRateLimitingFilter() {
            return new RateLimitingFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain)
                        throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth ->
                            auth.anyRequest().permitAll()
                    );
            return http.build();
        }
    }

    @Nested
    @DisplayName("POST /api/metas/{mid}/registros")
    class PostRecords {
        @Test
        public void postBoolRecord_whenValidRequest_201() throws Exception {
            boolRecordJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\": false "
                    + "}";
            when(recordService.createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid)))
                    .thenReturn(responseBoolGoalDTO);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolRecordJson));

            response.andExpect(status().isCreated());

            verify(recordService).createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenValidRequest_201() throws Exception {
            numRecordJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": 12.34 "
                    + "}";
            when(recordService.createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid)))
                    .thenReturn(responseNumGoalDTO);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numRecordJson));

            response.andExpect(status().isCreated());

            verify(recordService).createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenEmptyBody_400() throws Exception {
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("")  // cuerpo vacío
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(recordService, never()).createBoolRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenEmptyBody_400() throws Exception {
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("")  // cuerpo vacío
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(recordService, never()).createNumRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenUnknownPropertyInJson_400() throws Exception {
            String badJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\":true,"
                    + "\"unknownField\":123"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never()).createBoolRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenUnknownPropertyInJson_400() throws Exception {
            String badJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\":12.34,"
                    + "\"extra\":true"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never()).createNumRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postRecord_whenTipoNotSupported_400() throws Exception {
            String validJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\":true"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Other")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(validJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(recordService, never()).createBoolRecord(any(), eq(uid), eq(mid));
            verify(recordService, never()).createNumRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postRecord_whenMissingTipoParam_400() throws Exception {
            String validJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\":true"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(validJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verify(recordService, never()).createBoolRecord(any(), eq(uid), eq(mid));
            verify(recordService, never()).createNumRecord(any(), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenTipoBoolButBodyNum_400() throws Exception {
            String numJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": 12.34"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenTipoNumButBodyBool_400() throws Exception {
            String boolJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\": false"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenMissingFecha_400() throws Exception {
            String boolJson = "{"
                    + "\"valorBool\": false"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("La fecha no puede estar vacia"));  // from CreateRecordDTO.fecha :contentReference[oaicite:6]{index=6}

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenMissingValorBool_400() throws Exception {
            String boolJson = "{"
                    + "\"fecha\":\"2025-07-01\""
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenInvalidFechaFormat_400() throws Exception {
            String boolJson = "{"
                    + "\"fecha\":\"2025-99-99\","
                    + "\"valorBool\": true"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenValorBoolAsNumber_400() throws Exception {
            String badBoolJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\": 123"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badBoolJson)
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postBoolRecord_whenValorBoolAsString_400() throws Exception {
            String badBoolJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorBool\": \"notABoolean\""
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badBoolJson)
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createBoolRecord(any(CreateBoolRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenMissingFecha_400() throws Exception {
            String numJson = "{"
                    + "\"valorNum\": 12.34"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("La fecha no puede estar vacia"));  // from CreateRecordDTO.fecha :contentReference[oaicite:7]{index=7}

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenMissingValorNum_400() throws Exception {
            String numJson = "{"
                    + "\"fecha\":\"2025-07-01\""
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenNegativeValorNum_400() throws Exception {
            String numJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": -1.0"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("El valor del registro debe ser cero o positivo"));  // from CreateNumRecordDTO.valorNum :contentReference[oaicite:8]{index=8}

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenTooManyIntegerDigits_400() throws Exception {
            String numJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": 12345678901234.0"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));  // from CreateNumRecordDTO.valorNum :contentReference[oaicite:9]{index=9}

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenTooManyFractionDigits_400() throws Exception {
            String numJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": 123.456"
                    + "}";
            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));  // from CreateNumRecordDTO.valorNum :contentReference[oaicite:10]{index=10}

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenValorNumAsString_400() throws Exception {
            String badNumJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": \"notANumber\""
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badNumJson)
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void postNumRecord_whenValorNumAsBoolean_400() throws Exception {
            String badNumJson = "{"
                    + "\"fecha\":\"2025-07-01\","
                    + "\"valorNum\": true"
                    + "}";

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/registros", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badNumJson)
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado"));

            verify(recordService, never())
                    .createNumRecord(any(CreateNumRecordDTO.class), eq(uid), eq(mid));
        }
    }

    @Nested
    @DisplayName("DELETE /api/metas/{mid}/registros/{fecha}")
    class DeleteRecords {

        @Test
        public void deleteRecord_whenValidRequest_200() throws Exception {
            ResultActions response = mockMvc.perform(delete("/api/metas/{mid}/registros/{fecha}",
                    "goal123", "2025-07-18")
                    .with(authentication(defaultPrincipal))
                    .accept(MediaType.APPLICATION_JSON)
            );

            response.andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .string("Registro eliminado exitosamente"));

            verify(recordService).deleteRecord("test-uid", "goal123",
                    LocalDate.parse("2025-07-18")
            );
        }
    }
}