package org.GoLifeAPI.mixed.web;

import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.infrastructure.MongoService;
import org.GoLifeAPI.web.HealthRestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HealthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MongoService mongoService;
    @MockitoBean
    private FirebaseService firebaseService;

    @Nested
    @DisplayName("GET /api/salud")
    class GetSalud {

        @Test
        public void whenAllServicesUp_thenReturns200() throws Exception {
            when(mongoService.ping()).thenReturn(true);
            when(firebaseService.isAvailable()).thenReturn(true);

            mockMvc.perform(get("/api/salud")
                            .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }

        @Test
        public void whenMongoDown_thenReturns503() throws Exception {
            when(mongoService.ping()).thenReturn(false);
            when(firebaseService.isAvailable()).thenReturn(true);

            mockMvc.perform(get("/api/salud"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().string("Algunos servicios no estan disponibles"));
        }

        @Test
        public void whenFirebaseDown_thenReturns503() throws Exception {
            when(mongoService.ping()).thenReturn(true);
            when(firebaseService.isAvailable()).thenReturn(false);

            mockMvc.perform(get("/api/salud"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().string("Algunos servicios no estan disponibles"));
        }

        @Test
        public void whenException_thenReturns500() throws Exception {
            when(mongoService.ping()).thenThrow(new RuntimeException("DB down"));

            mockMvc.perform(get("/api/salud"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Comprobacion de salud fallida: Error Interno"));
        }
    }
}
