package org.GoLifeAPI.e2e;

import org.GoLifeAPI.util.MongoContainer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecordTestIT extends CommonE2EMockIT {

    //Main Happy Path

    @Order(1)
    @Test
    public void e2e_postBoolRecord_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"fecha\":\"2025-06-19\","
                + "\"valorBool\":false"
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/registros", MongoContainer.getBoolMid())
                .param("tipo", "Bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Bool"))
                .andExpect(jsonPath("$.registros").isNotEmpty())
                .andExpect(jsonPath("$.registros[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.registros[0].valorBool").value(false))
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(false));
    }

    @Order(2)
    @Test
    public void e2e_postNumRecord_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"fecha\":\"2025-06-19\","
                + "\"valorNum\":0"
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/registros", MongoContainer.getNumMid())
                .param("tipo", "Num")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Num"))
                .andExpect(jsonPath("$.registros").isNotEmpty())
                .andExpect(jsonPath("$.registros[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.registros[0].valorNum").value(0))
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(false));
    }

    @Order(3)
    @Test
    public void e2e_deleteBoolRecord_returns201_andJsonBody() throws Exception {

        ResultActions response = mockMvc.perform(
                delete("/api/metas/{mid}/registros/{fecha}", MongoContainer.getBoolMid(), "2025-06-19")
                        .param("tipo", "Bool")
                        .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(content().string("Registro eliminado exitosamente"));
    }

    @Order(4)
    @Test
    public void e2e_deleteNumRecord_returns201_andJsonBody() throws Exception {
        ResultActions response = mockMvc.perform(
                delete("/api/metas/{mid}/registros/{fecha}", MongoContainer.getNumMid(), "2025-06-19")
                        .param("tipo", "Num")
                        .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(content().string("Registro eliminado exitosamente"));
    }

    // Other Happy Path Tests

    @Test
    public void e2e_postBoolRecordValueReached_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"fecha\":\"2025-06-19\","
                + "\"valorBool\":true"
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/registros", MongoContainer.getBoolMid())
                .param("tipo", "Bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Bool"))
                .andExpect(jsonPath("$.registros").isNotEmpty())
                .andExpect(jsonPath("$.registros[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.registros[0].valorBool").value(true))
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(true));
    }

    @Test
    public void e2e_postNumRecordValueReached_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"fecha\":\"2025-06-19\","
                + "\"valorNum\":10"
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/registros", MongoContainer.getNumMid())
                .param("tipo", "Num")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("Num"))
                .andExpect(jsonPath("$.registros").isNotEmpty())
                .andExpect(jsonPath("$.registros[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.registros[0].valorNum").value(10))
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(true));
    }
}