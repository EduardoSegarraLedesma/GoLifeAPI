package org.GoLifeAPI.mixed.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.GoLifeAPI.config.ValidatorConfig;
import org.GoLifeAPI.dto.user.CreateUserDTO;
import org.GoLifeAPI.dto.user.PatchUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.mapper.service.GoalDtoMapper;
import org.GoLifeAPI.mapper.service.RecordDtoMapper;
import org.GoLifeAPI.mapper.service.UserDtoMapper;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.security.RateLimitingFilter;
import org.GoLifeAPI.service.interfaces.IUserService;
import org.GoLifeAPI.web.UserRestController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = UserRestController.class)
@AutoConfigureMockMvc
@Import({UserDtoMapper.class, GoalDtoMapper.class, RecordDtoMapper.class,
        UserRestControllerTest.TestSecurityConfig.class, ValidatorConfig.class})
@ImportAutoConfiguration({
        ValidationAutoConfiguration.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDtoMapper userDtoMapper;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean
    private IUserService userService;

    private UsernamePasswordAuthenticationToken defaultPrincipal;

    private CreateUserDTO createUserDTO;
    private PatchUserDTO patchUserDTO;
    private User user;
    private ResponseUserDTO responseUserDTO;

    @BeforeEach
    void setUp() {
        defaultPrincipal = new UsernamePasswordAuthenticationToken(
                "test-uid", null, Collections.emptyList()
        );
        clearInvocations(userService);
        user = new User("test-uid", "test-apellido", "test-nombre");
        responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);
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
    @DisplayName("POST /api/usuarios")
    class PostUsers {
        @Test
        public void postUser_whenValidRequest_201() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre("test-nombre");
            createUserDTO.setApellidos("test-apellido");

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value("test-nombre"))
                    .andExpect(jsonPath("$.apellidos").value("test-apellido"));
        }

        @Test
        public void postUser_whenEmptyBody_400() throws Exception {
            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("")
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        public void postUser_whenNameNull_400() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre(null);
            createUserDTO.setApellidos("test-apellido");

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message").value("El nombre es obligatorio"));
        }

        @Test
        public void postUser_whenNameBlank_400() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre(""); // 0 chars
            createUserDTO.setApellidos("test-apellido");

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message").exists());
        }

        @Test
        public void postUser_whenNameTooShort_400() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre("t");
            createUserDTO.setApellidos("test-apellido");

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message").value("El nombre debe tener entre 2 y 50 caracteres"));
        }

        @Test
        public void postUser_whenNameTooLong_400() throws Exception {
            createUserDTO = new CreateUserDTO();
            String longName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 51 chars
            createUserDTO.setNombre(longName);
            createUserDTO.setApellidos("test-apellido");

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message").value("El nombre debe tener entre 2 y 50 caracteres"));
        }

        @Test
        public void postUser_whenSurnameTooLong_400() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre("test-nombre");
            String longSurname = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"; // 51 chars
            createUserDTO.setApellidos(longSurname);

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message").value("Los apellidos deben tener entre 0 y 50 caracteres"));
        }

        @Test
        public void postUser_whenNameAtMinLengthAndSurnameNull_201() throws Exception {
            createUserDTO = new CreateUserDTO();
            createUserDTO.setNombre("ab");      // exactly 2 chars
            createUserDTO.setApellidos(null);   // null allowed by @Size(min=0)

            user = new User("test-uid", "", "ab");
            responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value("ab"))
                    .andExpect(jsonPath("$.apellidos").value(""));
        }

        @Test
        public void postUser_whenNameAtMaxLengthAndSurnameEmpty_201() throws Exception {
            createUserDTO = new CreateUserDTO();
            // exactly 50 chars
            String maxName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            createUserDTO.setNombre(maxName);
            createUserDTO.setApellidos("");      // empty allowed by @Size

            user = new User("test-uid", "", maxName);
            responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);

            when(userService.createUser(any(CreateUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(post("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(createUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value(maxName))
                    .andExpect(jsonPath("$.apellidos").value(""));
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios")
    class GetUsers {
        @Test
        public void getUser_whenValidRequest_200() throws Exception {
            when(userService.getUser("test-uid"))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(get("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .accept(MediaType.APPLICATION_JSON));

            verify(userService).getUser("test-uid");

            response.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value("test-nombre"))
                    .andExpect(jsonPath("$.apellidos").value("test-apellido"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/usuarios")
    class PatchUsers {
        @Test
        public void patchUser_whenValidRequest_200() throws Exception {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("test-nombre2");
            patchUserDTO.setApellidos("test-apellidos2");

            user = new User("test-uid", "test-apellidos2", "test-nombre2");
            ResponseUserDTO responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);

            when(userService.updateUser(any(PatchUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value("test-nombre2"))
                    .andExpect(jsonPath("$.apellidos").value("test-apellidos2"));
        }

        @Test
        public void patchUser_whenEmptyBody_400() throws Exception {
            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("")
            );

            response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        public void patchUser_whenNameBlank_400() throws Exception {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("");
            patchUserDTO.setApellidos("test-apellidos");

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message")
                    .value("El nombre debe tener entre 2 y 50 caracteres"));
        }

        @Test
        public void patchUser_whenNameTooShort_400() throws Exception {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("a");  // 1 char < min 2
            patchUserDTO.setApellidos("test-apellidos");

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message")
                    .value("El nombre debe tener entre 2 y 50 caracteres"));
        }

        @Test
        public void patchUser_whenNameTooLong_400() throws Exception {
            patchUserDTO = new PatchUserDTO();
            String longName = "a".repeat(51); // 51 chars > max 50
            patchUserDTO.setNombre(longName);
            patchUserDTO.setApellidos("test-apellidos");

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message")
                    .value("El nombre debe tener entre 2 y 50 caracteres"));
        }

        @Test
        public void patchUser_whenSurnameTooLong_400() throws Exception {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("test-nombre");
            String longSurname = "b".repeat(51); // 51 chars > max 50
            patchUserDTO.setApellidos(longSurname);

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isBadRequest());
            response.andExpect(jsonPath("$.message")
                    .value("Los apellidos deben tener entre 0 y 50 caracteres"));
        }

        @Test
        public void patchUser_whenNameAtMinLengthAndSurnameNull_200() throws Exception {
            patchUserDTO = new PatchUserDTO();
            patchUserDTO.setNombre("ab");      // exactly 2 chars
            patchUserDTO.setApellidos(null);   // null allowed

            user = new User("test-uid", "", "ab");
            responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);
            when(userService.updateUser(any(PatchUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value("ab"))
                    .andExpect(jsonPath("$.apellidos").value(""));
        }

        @Test
        public void patchUser_whenNameAtMaxLengthAndSurnameEmpty_200() throws Exception {
            patchUserDTO = new PatchUserDTO();
            String maxName = "a".repeat(50);  // exactly 50 chars
            patchUserDTO.setNombre(maxName);
            patchUserDTO.setApellidos("");     // empty allowed

            user = new User("test-uid", "", maxName);
            responseUserDTO = userDtoMapper.mapUserToResponseUserDTO(user);
            when(userService.updateUser(any(PatchUserDTO.class), eq("test-uid")))
                    .thenReturn(responseUserDTO);

            ResultActions response = mockMvc.perform(patch("/api/usuarios")
                    .with(authentication(defaultPrincipal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(patchUserDTO)));

            response.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nombre").value(maxName))
                    .andExpect(jsonPath("$.apellidos").value(""));
        }
    }

    @Nested
    @DisplayName("DELETE /api/usuarios")
    class DeleteUsers {
        @Test
        public void deleteUser_whenValidRequest_200() throws Exception {

            ResultActions response = mockMvc.perform(delete("/api/usuarios")
                    .with(authentication(defaultPrincipal))
            );

            response.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("Usuario eliminado exitosamente"));

            verify(userService).deleteUser("test-uid");
        }
    }
}