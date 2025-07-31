package org.GoLifeAPI.e2e;


import org.GoLifeAPI.Main;
import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.util.MongoContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        classes = Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(CommonE2EMockIT.TestFirebaseConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class CommonE2EMockIT {

    @Autowired
    protected MockMvc mockMvc;

    static {
        setEnv("DB_CONNECTION_STRING", MongoContainer.getMongoURI());
        setEnv("DB_NAME", "e2e");
        MongoContainer.setUid("test-user");
    }

    @TestConfiguration
    static class TestFirebaseConfig {
        @Bean
        public FirebaseService firebaseService() {
            FirebaseService mockFs = mock(FirebaseService.class);
            when(mockFs.isAvailable()).thenReturn(true);
            when(mockFs.verifyBearerToken("Bearer good.token")).thenReturn("test-user");
            when(mockFs.deleteFirebaseUser(anyString())).thenReturn(true);
            return mockFs;
        }
    }

    @SuppressWarnings("unchecked")
    private static void setEnv(String key, String value) {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field envField = pe.getDeclaredField("theEnvironment");
            envField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) envField.get(null);
            env.put(key, value);

            Field cienvField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            cienvField.setAccessible(true);
            ((Map<String, String>) cienvField.get(null)).put(key, value);

        } catch (NoSuchFieldException e) {
            try {
                Map<String, String> env = System.getenv();
                Field m = env.getClass().getDeclaredField("m");
                m.setAccessible(true);
                ((Map<String, String>) m.get(env)).put(key, value);
            } catch (Exception ex) {
                throw new IllegalStateException("No se pudo configurar la env var", ex);
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo configurar la env var", e);
        }
    }

    @Order(1)
    @Test
    public void e2e_healthCheck_allServicesUp_returns200() throws Exception {
        mockMvc.perform(get("/api/salud"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}