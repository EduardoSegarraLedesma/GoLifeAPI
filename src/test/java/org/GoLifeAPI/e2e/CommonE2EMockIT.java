package org.GoLifeAPI.e2e;


import org.GoLifeAPI.Main;
import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.infrastructure.MongoService;
import org.GoLifeAPI.persistence.implementation.dao.BaseDAO;
import org.GoLifeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLifeAPI.persistence.implementation.dao.UserDAO;
import org.GoLifeAPI.util.MongoContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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

        @Bean
        public MongoService mongoService() {
            try {
                AtomicReference<MongoService> ref = new AtomicReference<>();
                withEnvironmentVariable("DB_CONNECTION_STRING", MongoContainer.getMongoURI())
                        .execute(() -> {
                            MongoService real = new MongoService();
                            ReflectionTestUtils.invokeMethod(real, "initializeMongoService");
                            ref.set(spy(real));
                        });
                return ref.get();
            } catch (Exception e) {
                throw new IllegalStateException("No pude inicializar MongoService de prueba", e);
            }
        }

        @Bean
        public UserDAO userDAO(MongoService mongoService) throws Exception {
            AtomicReference<UserDAO> ref = new AtomicReference<>();
            withEnvironmentVariable("DB_NAME", "e2e")
                    .execute(() -> {
                        UserDAO real = new UserDAO(mongoService);
                        ref.set(spy(real));
                    });
            return ref.get();
        }

        @Bean
        public GoalDAO goalDAO(MongoService mongoService) throws Exception {
            AtomicReference<GoalDAO> ref = new AtomicReference<>();
            withEnvironmentVariable("DB_NAME", "e2e")
                    .execute(() -> {
                        GoalDAO real = new GoalDAO(mongoService);
                        ref.set(spy(real));
                    });
            return ref.get();
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