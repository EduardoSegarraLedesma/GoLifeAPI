package org.GoLifeAPI.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.GoLifeAPI.dto.goal.*;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLifeAPI.model.Enums;
import org.GoLifeAPI.security.RateLimitingFilter;
import org.GoLifeAPI.service.interfaces.IGoalService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers =GoalRestController.class)
@AutoConfigureMockMvc
@Import(GoalRestControllerTest.TestSecurityConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GoalRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean
    private IGoalService goalService;

    private UsernamePasswordAuthenticationToken defaultPrincipal;

    String uid;
    String mid;
    private String boolGoalJson;
    private String numGoalJson;
    private CreateBoolGoalDTO createBoolGoalDTO;
    private CreateNumGoalDTO createNumGoalDTO;
    private ResponseUserDTO responseUserDTO;
    private ResponseUserStatsDTO responseUserStatsDTO;

    @BeforeAll
    public void initialSetUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    public void setUp() {
        uid = "test-uid";
        mid = "test-mid";
        defaultPrincipal = new UsernamePasswordAuthenticationToken(
                uid, null, Collections.emptyList()
        );
        clearInvocations(goalService);
        createBoolGoalDTO = null;
        createNumGoalDTO = null;
        responseUserDTO = null;
        boolGoalJson = null;
        numGoalJson = null;
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean(name = "rateLimitingFilter")
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
    @DisplayName("CREATE /api/metas/bool")
    class CreateBoolGoals {

        @Test
        public void postMetaBool_whenValidRequest_201() throws Exception {
            CreateBoolGoalDTO createBoolGoalDTO = new CreateBoolGoalDTO("Actividad diaria",
                    "Hacer ejercicio cada mañana", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias
            );

            when(goalService.createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid)))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(createBoolGoalDTO))
            );

            response.andExpect(status().isCreated());

            verify(goalService).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenNombreNull_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO(null, "Desc", LocalDate.parse("2025-07-01"), 7, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre de la meta es obligatorio"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenNombreBlank_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("", "Desc", LocalDate.parse("2025-07-01"), 7, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre de la meta es obligatorio"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenNombreTooLong_400() throws Exception {
            String longName = "a".repeat(51);
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO(longName, "Desc", LocalDate.parse("2025-07-01"), 7, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre no puede superar los 50 caracteres"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenDescripcionTooLong_400() throws Exception {
            String longDesc = "a".repeat(301);
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("Nombre", longDesc, LocalDate.parse("2025-07-01"), 7, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La descripción no puede superar los 300 caracteres"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenFechaNull_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("Nombre", "Desc", null, 7, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La fecha de inicio es obligatoria"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenDuracionValorZero_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("Nombre", "Desc", LocalDate.parse("2025-07-01"), 0, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración no puede ser negativa ni cero"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenDuracionValorTooLarge_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("Nombre", "Desc", LocalDate.parse("2025-07-01"), 10001, Enums.Duracion.Dias);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración es demasiado grande, maximo 10000"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaBool_whenDuracionUnidadNull_400() throws Exception {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO("Nombre", "Desc", LocalDate.parse("2025-07-01"), 7, null);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/bool")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad de duración es obligatoria"));

            verify(goalService, never()).createBoolGoal(any(CreateBoolGoalDTO.class), eq(uid));
        }

    }

    @Nested
    @DisplayName("CREATE /api/metas/num")
    class CreateNumGoals {
        @Test
        public void postMetaNum_whenValidRequest_201() throws Exception {
            CreateNumGoalDTO createNumGoalDTO = new CreateNumGoalDTO("Actividad diaria",
                    "Hacer ejercicio cada mañana", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias, 123.45, "km"
            );

            when(goalService.createNumGoal(any(CreateNumGoalDTO.class), eq(uid)))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(createNumGoalDTO))
            );

            response.andExpect(status().isCreated());

            verify(goalService).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenNombreNull_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    null, "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre de la meta es obligatorio"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenNombreBlank_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre de la meta es obligatorio"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenNombreTooLong_400() throws Exception {
            String longName = "a".repeat(51);
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    longName, "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre no puede superar los 50 caracteres"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenDescripcionTooLong_400() throws Exception {
            String longDesc = "a".repeat(301);
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", longDesc, LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La descripción no puede superar los 300 caracteres"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenFechaNull_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", null, 7,
                    Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La fecha de inicio es obligatoria"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenDuracionValorZero_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    0, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración no puede ser negativa ni cero"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenDuracionValorTooLarge_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    10001, Enums.Duracion.Dias, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración es demasiado grande, maximo 10000"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenDuracionUnidadNull_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, null, 1.23, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad de duración es obligatoria"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenValorObjetivoNull_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    null, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El valor objetivo es obligatorio"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenValorObjetivoNegative_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    -1.0, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El valor objetivo debe ser un número positivo"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenValorObjetivoZero_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    0.0, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El valor objetivo debe ser un número positivo"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenValorObjetivoIntegerTooMany_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    12345678901234.0, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenValorObjetivoFractionTooMany_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    1.234, "km"
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenUnidadNull_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    123.45, null
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad es obligatoria"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenUnidadBlank_400() throws Exception {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    123.45, ""
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad es obligatoria"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }

        @Test
        public void postMetaNum_whenUnidadTooLong_400() throws Exception {
            String longUnit = "a".repeat(21);
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "Nombre", "Desc", LocalDate.parse("2025-07-01"),
                    7, Enums.Duracion.Dias,
                    123.45, longUnit
            );
            ResultActions response = mockMvc.perform(
                    post("/api/metas/num")
                            .with(authentication(defaultPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto))
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad no debe tener más de 20 caracteres"));
            verify(goalService, never()).createNumGoal(any(CreateNumGoalDTO.class), eq(uid));
        }


    }

    @Nested
    @DisplayName("GET /api/metas/{mid}")
    class GetGoals {
        @Test
        public void getMeta_whenValidRequest_200() throws Exception {
            when(goalService.getGoal(eq(uid), eq(mid)))
                    .thenReturn(ResponseBoolGoalDTO.class);

            ResultActions response = mockMvc.perform(get("/api/metas/{mid}", mid)
                    .with(authentication(defaultPrincipal))
                    .accept(MediaType.APPLICATION_JSON)
            );

            response.andExpect(status().isOk());

            verify(goalService).getGoal(uid, mid);
        }
    }

    @Nested
    @DisplayName("PATCH /api/metas/{mid}")
    class PatchGoals {

        @Test
        public void patchMeta_whenValidBool_200() throws Exception {
            boolGoalJson = "{"
                    + "\"nombre\":\"Nueva Meta Bool\","
                    + "\"descripcion\":\"Descripción actualizada\","
                    + "\"duracionValor\":10,"
                    + "\"duracionUnidad\":\"Dias\""
                    + "}";

            when(goalService.updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid)))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(boolGoalJson)
            );

            response.andExpect(status().isOk());

            verify(goalService).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenValidNum_200() throws Exception {
            numGoalJson = "{"
                    + "\"nombre\":\"Nueva Meta Num\","
                    + "\"descripcion\":\"Desc actualizada\","
                    + "\"duracionValor\":15,"
                    + "\"duracionUnidad\":\"Dias\","
                    + "\"valorObjetivo\":123.45,"
                    + "\"unidad\":\"km\""
                    + "}";

            when(goalService.updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid)))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numGoalJson)
            );

            response.andExpect(status().isOk());

            verify(goalService).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenEmptyJsonBody_400() throws Exception {
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("{}")
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El cuerpo no puede estar vacío"));

            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenMalformedJson_400() throws Exception {
            String badJson = "{ \"nombre\":\"Meta\", \"duracionValor\":10 "; // truncado
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cuerpo de la petición malformado o inválido"));

            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenTipoBoolButBodyNum_400() throws Exception {
            String numJson = "{"
                    + "\"valorObjetivo\":5.0,"
                    + "\"unidad\":\"km\""
                    + "}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(numJson)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado o invalido"));

            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenTipoBoolWithUnknownProperty_400() throws Exception {
            String badJson = "{"
                    + "\"foo\":123,"
                    + "\"bar\":\"xyz\""
                    + "}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badJson)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cuerpo de la petición malformado o invalido"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenTipoNumWithUnknownProperty_400() throws Exception {
            String badJson = "{"
                    + "\"extraField\":true,"
                    + "\"another\":0"
                    + "}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(badJson)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado o invalido"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMeta_whenTipoInvalid_400() throws Exception {
            String json = "{\"nombre\":\"Actualizado\"}";

            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "InvalidTipo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );

            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(goalService);
        }

        @Test
        public void patchMetaBool_whenNombreTooLong_400() throws Exception {
            String longName = "a".repeat(51);
            String json = "{\"nombre\":\"" + longName + "\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre no puede superar los 50 caracteres"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaBool_whenDescripcionTooLong_400() throws Exception {
            String longDesc = "a".repeat(301);
            String json = "{\"descripcion\":\"" + longDesc + "\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La descripción no puede superar los 300 caracteres"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaBool_whenDuracionValorZero_400() throws Exception {
            String json = "{\"duracionValor\":0}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración no puede ser negativa ni cero"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaBool_whenDuracionValorTooLarge_400() throws Exception {
            String json = "{\"duracionValor\":10001}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración es demasiado grande, maximo 10000"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaBool_whenInvalidDuracionUnidad_400() throws Exception {
            String json = "{\"duracionUnidad\":\"InvalidDuracion\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Bool")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado o invalido"));
            verify(goalService, never()).updateBoolGoal(any(PatchBoolGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenNombreTooLong_400() throws Exception {
            String longName = "a".repeat(51);
            String json = "{\"nombre\":\"" + longName + "\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre no puede superar los 50 caracteres"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenDescripcionTooLong_400() throws Exception {
            String longDesc = "a".repeat(301);
            String json = "{\"descripcion\":\"" + longDesc + "\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La descripción no puede superar los 300 caracteres"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenDuracionValorZero_400() throws Exception {
            String json = "{\"duracionValor\":0}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración no puede ser negativa ni cero"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenDuracionValorTooLarge_400() throws Exception {
            String json = "{\"duracionValor\":10001}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La duración es demasiado grande, maximo 10000"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenInvalidDuracionUnidad_400() throws Exception {
            String json = "{\"duracionUnidad\":\"WrongValue\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cuerpo de la petición malformado o invalido"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenValorObjetivoNegative_400() throws Exception {
            String json = "{\"valorObjetivo\":-1.0}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El valor objetivo debe ser un número positivo"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenValorObjetivoZero_400() throws Exception {
            String json = "{\"valorObjetivo\":0.0}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El valor objetivo debe ser un número positivo"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenValorObjetivoTooManyIntegers_400() throws Exception {
            String json = "{\"valorObjetivo\":12345678901234.0}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenValorObjetivoTooManyFractions_400() throws Exception {
            String json = "{\"valorObjetivo\":1.234}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Formato inválido: máximo 13 cifras enteras y 2 decimales"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }

        @Test
        public void patchMetaNum_whenUnidadTooLong_400() throws Exception {
            String longUnit = "a".repeat(21);
            String json = "{\"unidad\":\"" + longUnit + "\"}";
            ResultActions response = mockMvc.perform(
                    patch("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .param("tipo", "Num")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(json)
            );
            response.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La unidad no debe tener más de 20 caracteres"));
            verify(goalService, never()).updateNumGoal(any(PatchNumGoalDTO.class), eq(uid), eq(mid));
        }
    }

    @Nested
    @DisplayName("POST /api/metas/finalizar")
    class FinalizeGoals {
        @Test
        public void postMetaFinalize_whenValidRequest_200() throws Exception {

            when(goalService.finalizeGoal(uid, mid))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(
                    post("/api/metas/{mid}/finalizar", mid)
                            .with(authentication(defaultPrincipal))
                            .accept(MediaType.APPLICATION_JSON)
            );

            response.andExpect(status().isOk());

            verify(goalService).finalizeGoal(uid, mid);
        }
    }

    @Nested
    @DisplayName("DELETE /api/metas/{mid}")
    class DeleteGoals {
        @Test
        public void deleteMeta_whenValidRequest_200() throws Exception {
            when(goalService.deleteGoal(eq(uid), eq(mid)))
                    .thenReturn(responseUserStatsDTO);

            ResultActions response = mockMvc.perform(
                    delete("/api/metas/{mid}", mid)
                            .with(authentication(defaultPrincipal))
                            .accept(MediaType.APPLICATION_JSON)
            );

            response.andExpect(status().isOk());

            verify(goalService).deleteGoal(uid, mid);
        }
    }
}