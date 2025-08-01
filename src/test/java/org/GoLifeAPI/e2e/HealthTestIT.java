package org.GoLifeAPI.e2e;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HealthTestIT extends CommonE2EMockIT {

    @Order(1)
    @Test
    public void e2e_healthCheck_allServicesUp_returns200() throws Exception {
        mockMvc.perform(get("/api/salud"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
