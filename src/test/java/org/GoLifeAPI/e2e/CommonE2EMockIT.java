package org.GoLifeAPI.e2e;


import org.GoLifeAPI.Main;
import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.infrastructure.KeyManagementService;
import org.GoLifeAPI.infrastructure.MongoService;
import org.GoLifeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLifeAPI.persistence.implementation.dao.UserDAO;
import org.GoLifeAPI.util.MongoContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(
        classes = Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(CommonE2EMockIT.E2ETestConfig.class)
public abstract class CommonE2EMockIT {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected FirebaseService firebaseService;
    @MockitoBean
    protected KeyManagementService keyManagementService;

    static {
        MongoContainer.setUid("test-user");
    }

    @BeforeEach
    public void stubServices() {
        when(firebaseService.isAvailable()).thenReturn(true);
        when(firebaseService.verifyBearerToken("Bearer good.token")).thenReturn("test-user");
        when(firebaseService.deleteFirebaseUser(anyString())).thenReturn(true);
        when(keyManagementService.ping()).thenReturn(true);
        when(keyManagementService.sign("test-user")).thenReturn("signed-test-user");
        when(keyManagementService.verify("test-user", "signed-test-user")).thenReturn(true);
    }

    @TestConfiguration
    static class E2ETestConfig {
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
}