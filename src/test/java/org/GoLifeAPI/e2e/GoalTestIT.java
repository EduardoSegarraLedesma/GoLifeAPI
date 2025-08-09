package org.GoLifeAPI.e2e;

import org.GoLifeAPI.util.MongoContainer;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GoalTestIT extends CommonE2EMockIT {


    @AfterAll
    public void nextTestSetUp() throws Exception {
        when(firebaseService.isAvailable()).thenReturn(true);
        when(firebaseService.verifyBearerToken(any())).thenReturn("test-user");
        when(firebaseService.deleteFirebaseUser(anyString())).thenReturn(true);
        String payload = "{"
                + "\"nombre\":\"Beber 2 litros de agua\","
                + "\"descripcion\":\"Recordar beber suficiente agua diariamente\","
                + "\"fecha\":\"2025-06-19\","
                + "\"duracionValor\":30,"
                + "\"duracionUnidad\":\"Dias\""
                + "}";
        ResultActions response = mockMvc.perform(post("/api/metas/bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
        MvcResult mvcResult = response.andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        String metaId = com.jayway.jsonpath.JsonPath
                .parse(json)
                .read("$.metas[1]._id", String.class);
        MongoContainer.setBoolMid(metaId);


        payload = "{"
                + "\"nombre\":\"Beber agua\","
                + "\"descripcion\":\"Beber al menos 2 litros de agua al día\","
                + "\"fecha\":\"2025-06-19\","
                + "\"duracionValor\":7,"
                + "\"duracionUnidad\":\"Dias\","
                + "\"valorObjetivo\":2,"
                + "\"unidad\":\"litros\""
                + "}";
         response = mockMvc.perform(post("/api/metas/num")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
         mvcResult = response.andReturn();
         json = mvcResult.getResponse().getContentAsString();
         metaId = com.jayway.jsonpath.JsonPath
                .parse(json)
                .read("$.metas[2]._id", String.class);
        MongoContainer.setNumMid(metaId);
    }

    //Main Happy Path

    @Order(1)
    @Test
    public void e2e_postBoolGoal_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"nombre\":\"Beber 2 litros de agua\","
                + "\"descripcion\":\"Recordar beber suficiente agua diariamente\","
                + "\"fecha\":\"2025-06-19\","
                + "\"duracionValor\":30,"
                + "\"duracionUnidad\":\"Dias\""
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.metas").isNotEmpty())
                .andExpect(jsonPath("$.metas[0].nombre").value("Beber 2 litros de agua"))
                .andExpect(jsonPath("$.metas[0].tipo").value("Bool"))
                .andExpect(jsonPath("$.metas[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.metas[0].finalizado").value(false))
                .andExpect(jsonPath("$.metas[0].duracionValor").value(30))
                .andExpect(jsonPath("$.metas[0].duracionUnidad").value("Dias"))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(1))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));

        MvcResult mvcResult = response.andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        String metaId = com.jayway.jsonpath.JsonPath
                .parse(json)
                .read("$.metas[0]._id", String.class);
        MongoContainer.setBoolMid(metaId);
    }

    @Order(2)
    @Test
    public void e2e_postNumGoal_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"nombre\":\"Beber agua\","
                + "\"descripcion\":\"Beber al menos 2 litros de agua al día\","
                + "\"fecha\":\"2025-06-19\","
                + "\"duracionValor\":7,"
                + "\"duracionUnidad\":\"Dias\","
                + "\"valorObjetivo\":2,"
                + "\"unidad\":\"litros\""
                + "}";


        ResultActions response = mockMvc.perform(post("/api/metas/num")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.metas").isNotEmpty())
                .andExpect(jsonPath("$.metas[1].nombre").value("Beber agua"))
                .andExpect(jsonPath("$.metas[1].tipo").value("Num"))
                .andExpect(jsonPath("$.metas[1].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.metas[1].finalizado").value(false))
                .andExpect(jsonPath("$.metas[1].duracionValor").value(7))
                .andExpect(jsonPath("$.metas[1].duracionUnidad").value("Dias"))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(2))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));

        MvcResult mvcResult = response.andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        String metaId = com.jayway.jsonpath.JsonPath
                .parse(json)
                .read("$.metas[1]._id", String.class);
        MongoContainer.setNumMid(metaId);
    }

    @Order(3)
    @Test
    public void e2e_patchBoolGoal_returns200_andJsonBody() throws Exception {
        String payload = "{"
                + "\"nombre\":\"Nuevo Nombre\","
                + "\"descripcion\":\"\","
                + "\"duracionValor\":6,"
                + "\"duracionUnidad\":\"Semanas\""
                + "}";

        ResultActions response = mockMvc.perform(patch("/api/metas/{mid}", MongoContainer.getBoolMid())
                .param("tipo", "Bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.metas").isNotEmpty())
                .andExpect(jsonPath("$.metas[0].nombre").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.metas[0].tipo").value("Bool"))
                .andExpect(jsonPath("$.metas[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.metas[0].finalizado").value(false))
                .andExpect(jsonPath("$.metas[0].duracionValor").value(6))
                .andExpect(jsonPath("$.metas[0].duracionUnidad").value("Semanas"))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(2))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));
    }

    @Order(4)
    @Test
    public void e2e_patchNumGoal_returns200_andJsonBody() throws Exception {
        String payload = "{"
                + "\"nombre\":\"Nuevo Nombre\","
                + "\"descripcion\":\"\","
                + "\"duracionValor\":6,"
                + "\"duracionUnidad\":\"Semanas\","
                + "\"valorObjetivo\":3,"
                + "\"unidad\":\"Cosas\""
                + "}";

        ResultActions response = mockMvc.perform(patch("/api/metas/{mid}", MongoContainer.getNumMid())
                .param("tipo", "Num")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.metas").isNotEmpty())
                .andExpect(jsonPath("$.metas[1].nombre").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.metas[1].tipo").value("Num"))
                .andExpect(jsonPath("$.metas[1].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.metas[1].finalizado").value(false))
                .andExpect(jsonPath("$.metas[1].duracionValor").value(6))
                .andExpect(jsonPath("$.metas[1].duracionUnidad").value("Semanas"))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(2))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));
    }

    @Order(5)
    @Test
    public void e2e_getBoolGoal_returns200_andJsonBody() throws Exception {

        ResultActions response = mockMvc.perform(get("/api/metas/{mid}", MongoContainer.getBoolMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.descripcion").value(""))
                .andExpect(jsonPath("$.tipo").value("Bool"))
                .andExpect(jsonPath("$.fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.finalizado").value(false))
                .andExpect(jsonPath("$.duracionValor").value(6))
                .andExpect(jsonPath("$.duracionUnidad").value("Semanas"))
                .andExpect(jsonPath("$.registros").isEmpty())
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(false))
                .andExpect(jsonPath("$.estadisticas.fechaFin").value("2025-07-31"));
    }

    @Order(6)
    @Test
    public void e2e_getNumGoal_returns200_andJsonBody() throws Exception {

        ResultActions response = mockMvc.perform(get("/api/metas/{mid}", MongoContainer.getNumMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.descripcion").value(""))
                .andExpect(jsonPath("$.tipo").value("Num"))
                .andExpect(jsonPath("$.fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.finalizado").value(false))
                .andExpect(jsonPath("$.duracionValor").value(6))
                .andExpect(jsonPath("$.duracionUnidad").value("Semanas"))
                .andExpect(jsonPath("$.valorObjetivo").value("3.0"))
                .andExpect(jsonPath("$.unidad").value("Cosas"))
                .andExpect(jsonPath("$.registros").isEmpty())
                .andExpect(jsonPath("$.estadisticas.valorAlcanzado").value(false))
                .andExpect(jsonPath("$.estadisticas.fechaFin").value("2025-07-31"));
    }

    @Order(7)
    @Test
    public void e2e_finalizeBoolGoal_returns200_andJsonBody() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/finalizar", MongoContainer.getBoolMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.metas[0].finalizado").value(true))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(2))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(1))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(50));
    }

    @Order(8)
    @Test
    public void e2e_finalizeNumGoal_returns200_andJsonBody() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/metas/{mid}/finalizar", MongoContainer.getNumMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.metas[1].finalizado").value(true))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(2))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(2))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(100));
    }

    @Order(9)
    @Test
    public void e2e_deleteBoolGoal_returns200_andJsonBody() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/metas/{mid}", MongoContainer.getBoolMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMetas").value(1))
                .andExpect(jsonPath("$.totalMetasFinalizadas").value(1))
                .andExpect(jsonPath("$.porcentajeFinalizadas").value(100));
    }

    @Order(10)
    @Test
    public void e2e_deleteNumGoal_returns200_andJsonBody() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/metas/{mid}", MongoContainer.getNumMid())
                .header("Authorization", "Bearer good.token"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMetas").value(0))
                .andExpect(jsonPath("$.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.porcentajeFinalizadas").value(0));
    }

    // Other Happy Path Tests

    @Test
    public void e2e_postBoolGoalIndefinido_returns201_andJsonBody() throws Exception {
        String payload = "{"
                + "\"nombre\":\"Beber 2 litros de agua\","
                + "\"descripcion\":\"Recordar beber suficiente agua diariamente\","
                + "\"fecha\":\"2025-06-19\","
                + "\"duracionValor\":1,"
                + "\"duracionUnidad\":\"Indefinido\""
                + "}";

        ResultActions response = mockMvc.perform(post("/api/metas/bool")
                .header("Authorization", "Bearer good.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.metas").isNotEmpty())
                .andExpect(jsonPath("$.metas[0].nombre").value("Beber 2 litros de agua"))
                .andExpect(jsonPath("$.metas[0].tipo").value("Bool"))
                .andExpect(jsonPath("$.metas[0].fecha").value("2025-06-19"))
                .andExpect(jsonPath("$.metas[0].finalizado").value(false))
                .andExpect(jsonPath("$.metas[0].duracionValor").value(-1))
                .andExpect(jsonPath("$.metas[0].duracionUnidad").value("Indefinido"))
                .andExpect(jsonPath("$.estadisticas.totalMetas").value(1))
                .andExpect(jsonPath("$.estadisticas.totalMetasFinalizadas").value(0))
                .andExpect(jsonPath("$.estadisticas.porcentajeFinalizadas").value(0));
    }
}