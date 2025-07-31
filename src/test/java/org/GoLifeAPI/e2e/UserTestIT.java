package org.GoLifeAPI.e2e;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UserTestIT extends CommonE2EMockIT {

    @Order(1)
    @Test
    public void e2e_postUsuario_returns201_andJsonBody() throws Exception {
        String payload = """
                {
                  "nombre": "test-nombre",
                  "apellidos": "test-apellidos"
                }
                """;

        ResultActions response = mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("test-nombre"))
                .andExpect(jsonPath("$.apellidos").value("test-apellidos"))
                .andExpect(jsonPath("$.metas").isEmpty())
                .andExpect(jsonPath("$.estadisticas").isNotEmpty());
    }
}
