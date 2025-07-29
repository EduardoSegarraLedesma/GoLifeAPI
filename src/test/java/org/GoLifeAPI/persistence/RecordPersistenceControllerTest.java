package org.GoLifeAPI.persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.GoLIfeAPI.mapper.persistence.GoalDocMapper;
import org.GoLIfeAPI.mapper.persistence.RecordDocMapper;
import org.GoLIfeAPI.persistence.implementation.RecordPersistenceController;
import org.GoLIfeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class RecordPersistenceControllerTest {

    private MongoService mongoService;
    private TransactionRunner transactionRunner;
    private GoalDocMapper goalDocMapper;
    private RecordDocMapper recordDocMapper;
    private GoalDAO goalDAO;

    @InjectMocks
    private RecordPersistenceController recordPersistenceController;

    String uid;
    String boolMid;
    String numMid;
    String boolDate;
    String numDate;

    @BeforeAll
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        String mongoUri = MongoContainer.getMongoURI();
        withEnvironmentVariable("DB_NAME", "test-db")
                .and("DB_CONNECTION_STRING", mongoUri)
                .execute(() -> {
                    mongoService = spy(new MongoService());
                    ReflectionTestUtils.invokeMethod(mongoService, "initializeMongoService");
                    transactionRunner = spy(new TransactionRunner(mongoService));
                    recordDocMapper = spy(new RecordDocMapper());
                    goalDocMapper = spy(new GoalDocMapper(recordDocMapper));
                    goalDAO = spy(new GoalDAO(mongoService));
                });
        ReflectionTestUtils.setField(recordPersistenceController, "transactionRunner", transactionRunner);
        ReflectionTestUtils.setField(recordPersistenceController, "recordDocMapper", recordDocMapper);
        ReflectionTestUtils.setField(recordPersistenceController, "goalDocMapper", goalDocMapper);
        ReflectionTestUtils.setField(recordPersistenceController, "goalDAO", goalDAO);
        uid = MongoContainer.getUid();
        boolMid = MongoContainer.getBoolMid();
        numMid = MongoContainer.getNumMid();
    }

    @BeforeEach
    public void beforeEach() {
        doCallRealMethod().when(transactionRunner).run(any());

        doCallRealMethod().when(goalDAO).insertRecordInListByGoalId(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(goalDAO).removeRecordFromListByGoalIdAndDate(any(ClientSession.class), any(String.class), any(String.class));

        clearInvocations(goalDAO);
    }

    @Order(1)
    @Nested
    @DisplayName("createBoolrecord")
    class CreateBoolrecord {

    }

    @Order(2)
    @Nested
    @DisplayName("createNumRecord")
    class CreateNumRecord {

    }

    @Order(3)
    @Nested
    @DisplayName("delete")
    class Delete {

    }
}