package org.GoLifeAPI.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.mapper.service.GoalDtoMapper;
import org.GoLIfeAPI.mapper.service.RecordDtoMapper;
import org.GoLIfeAPI.mapper.service.UserDtoMapper;
import org.GoLIfeAPI.service.interfaces.IGoalService;
import org.GoLIfeAPI.web.GoalRestController;
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

import static org.mockito.Mockito.clearInvocations;

@WebMvcTest(controllers = GoalRestController.class)
@AutoConfigureMockMvc
@Import({UserDtoMapper.class, GoalDtoMapper.class, RecordDtoMapper.class,
        GoalRestControllerTest.TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GoalRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDtoMapper userDtoMapper;
    @Autowired
    private GoalDtoMapper goaldDtoMapper;
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
    private ResponseBoolGoalDTO responseBoolGoalDTO;
    private ResponseNumGoalDTO responseNumGoalDTO;

    @BeforeEach
    public void setUp() {
        uid = "test-uid";
        mid = "test-mid";
        defaultPrincipal = new UsernamePasswordAuthenticationToken(
                uid, null, Collections.emptyList()
        );
        clearInvocations(goalService);
        boolGoalJson = null;
        numGoalJson = null;
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
    @DisplayName("CREATE /api/metas/bool")
    class CreateBoolGoals {

    }

    @Nested
    @DisplayName("CREATE /api/metas/num")
    class CreateNumGoals {

    }

    @Nested
    @DisplayName("GET /api/metas/{mid}")
    class GetGoals {

    }

    @Nested
    @DisplayName("PATCH /api/metas/{mid}")
    class PatchGoals {

    }

    @Nested
    @DisplayName("POST /api/metas/finalizar")
    class FinalizeGoals {

    }

    @Nested
    @DisplayName("DELETE /api/metas/{mid}")
    class DeleteGoals {

    }
}
