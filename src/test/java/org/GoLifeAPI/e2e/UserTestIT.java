package org.GoLifeAPI.e2e;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTestIT extends CommonE2EMockIT {

    @AfterAll
    public void nextTestSetUp() throws Exception {
        when(firebaseService.isAvailable()).thenReturn(true);
        when(firebaseService.verifyBearerToken(any())).thenReturn("test-user");
        when(firebaseService.deleteFirebaseUser(anyString())).thenReturn(true);
        String payload = "{\"nombre\":\"test-nombre\",\"apellidos\":\"test-apellidos\"}";
        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    //Main Happy Path

    @Order(1)
    @Test
    public void e2e_postUsuario_returns201_andJsonBody() throws Exception {
        String payload = "{\"nombre\":\"test-nombre\",\"apellidos\":\"test-apellidos\"}";

        ResultActions response = mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("test-nombre"))
                .andExpect(jsonPath("$.apellidos").value("test-apellidos"))
                .andExpect(jsonPath("$.metas").isEmpty())
                .andExpect(jsonPath("$.estadisticas").isNotEmpty())
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(0))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));
    }

    @Order(2)
    @Test
    public void e2e_patchUsuario_returns200_andJsonBody() throws Exception {
        String payload = "{\"nombre\":\"test-nombre2\",\"apellidos\":\"\"}";

        ResultActions response = mockMvc.perform(patch("/api/usuarios")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-nombre2"))
                .andExpect(jsonPath("$.apellidos").value(""))
                .andExpect(jsonPath("$.metas").isEmpty())
                .andExpect(jsonPath("$.estadisticas").isNotEmpty());
    }

    @Order(3)
    @Test
    public void e2e_getUsuario_returns200_andJsonBody() throws Exception {

        ResultActions response = mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-nombre2"))
                .andExpect(jsonPath("$.apellidos").value(""))
                .andExpect(jsonPath("$.metas").isEmpty())
                .andExpect(jsonPath("$.estadisticas").isNotEmpty());
    }

    @Order(4)
    @Test
    public void e2e_deleteUsuario_returns200_andMessage() throws Exception {

        ResultActions response = mockMvc.perform(delete("/api/usuarios")
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(content().string("Usuario eliminado exitosamente"));
    }
}