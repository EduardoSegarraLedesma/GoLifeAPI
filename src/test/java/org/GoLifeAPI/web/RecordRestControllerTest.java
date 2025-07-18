package org.GoLifeAPI.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.mapper.service.GoalDtoMapper;
import org.GoLIfeAPI.mapper.service.RecordDtoMapper;
import org.GoLIfeAPI.mapper.service.UserDtoMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.service.interfaces.IRecordService;
import org.GoLIfeAPI.web.RecordRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

@WebMvcTest(controllers = RecordRestController.class)
@AutoConfigureMockMvc
@Import({GoalDtoMapper.class, RecordDtoMapper.class,
        RecordRestControllerTest.TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecordRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoalDtoMapper goaldDtoMapper;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean
    private IRecordService recordService;

    private UsernamePasswordAuthenticationToken defaultPrincipal;

    private CreateBoolRecordDTO createBoolRecordDTO;
    private CreateNumRecordDTO createNumRecordDTO;
    private NumGoal numGoal;
    private BoolGoal boolGoal;
    private ResponseBoolGoalDTO responseBoolGoalDTO;
    private ResponseNumGoalDTO responseNumGoalDTO;

    @BeforeEach
    public void setUp() {
        defaultPrincipal = new UsernamePasswordAuthenticationToken(
                "test-uid", null, Collections.emptyList()
        );
    }

    @TestConfiguration
    static class TestSecurityConfig {
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
    }

    @Nested
    @DisplayName("POST /api/metas/{mid}/registros/{fecha}")
    class DeleteRecords {
    }
}