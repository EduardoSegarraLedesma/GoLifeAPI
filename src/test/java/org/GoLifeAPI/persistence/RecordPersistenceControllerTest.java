package org.GoLifeAPI.persistence;

import com.mongodb.client.ClientSession;
import org.GoLifeAPI.exception.NotFoundException;
import org.GoLifeAPI.infrastructure.MongoService;
import org.GoLifeAPI.mapper.persistence.GoalDocMapper;
import org.GoLifeAPI.mapper.persistence.RecordDocMapper;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.model.record.BoolRecord;
import org.GoLifeAPI.model.record.NumRecord;
import org.GoLifeAPI.persistence.implementation.RecordPersistenceController;
import org.GoLifeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLifeAPI.persistence.implementation.transaction.TransactionRunner;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

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
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateBoolrecord {

        @Order(1)
        @Test
        public void createBoolrecord_whenStatsUpdateNull_skipsStatsUpdate() {
            LocalDate date = LocalDate.of(2025, 7, 4);
            BoolRecord record = new BoolRecord(false, date);


            BoolGoal result = recordPersistenceController.createBoolrecord(record, null, boolMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).hasSize(1);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isFalse();
            BoolRecord inserted = result.getRegistros().get(0);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.isValorBool()).isFalse();

            verify(goalDAO, never()).updateGoalSatsByGoalId(any(), anyString(), any());
        }

        @Order(2)
        @Test
        public void createBoolrecord_whenStatsUpdateEmpty_skipsStatsUpdate() {
            LocalDate date = LocalDate.of(2025, 7, 3);
            BoolRecord record = new BoolRecord(false, date);

            BoolGoal result = recordPersistenceController.createBoolrecord(record, new Document(), boolMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).hasSize(2);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isFalse();
            BoolRecord inserted = result.getRegistros().get(1);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.isValorBool()).isFalse();

            verify(goalDAO, never()).updateGoalSatsByGoalId(any(), anyString(), any());
        }

        @Order(3)
        @Test
        public void createBoolrecord_whenValid_insertsRecordAndReturnsGoal() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            BoolRecord record = new BoolRecord(true, date);
            Document statsUpdate = new Document("valorAlcanzado", true);

            BoolGoal result = recordPersistenceController.createBoolrecord(record, statsUpdate, boolMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).isNotEmpty();
            Assertions.assertThat(result.getRegistros()).hasSize(3);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isTrue();
            BoolRecord inserted = result.getRegistros().get(2);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.isValorBool()).isTrue();

            boolDate = inserted.getFecha().toString();
        }

        @Test
        public void createBoolrecord_whenInsertRecordReturnsNull_throwsNotFoundException() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            BoolRecord record = new BoolRecord(true, date);
            Document statsUpdate = new Document("valorAlcanzado", true);

            doReturn(null)
                    .when(goalDAO).insertRecordInListByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.createBoolrecord(record, statsUpdate, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta del registro no encontrada");
        }

        @Test
        public void createBoolrecord_whenUpdateStatsReturnsNull_throwsNotFoundException() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            BoolRecord record = new BoolRecord(true, date);
            Document statsUpdate = new Document("valorAlcanzado", true);
            Document dummyGoalDoc = new Document();

            doReturn(dummyGoalDoc)
                    .when(goalDAO).insertRecordInListByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));
            doReturn(null)
                    .when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(boolMid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.createBoolrecord(record, statsUpdate, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta del registro no encontrada");
        }
    }

    @Order(2)
    @Nested
    @DisplayName("createNumRecord")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateNumRecord {

        @Order(1)
        @Test
        public void createNumRecord_whenStatsUpdateNull_skipsStatsUpdate() {
            LocalDate date = LocalDate.of(2025, 7, 4);
            double valor = 100.0;
            NumRecord record = new NumRecord(valor, date);

            NumGoal result = recordPersistenceController.createNumRecord(record, null, numMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).hasSize(1);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isFalse();
            NumRecord inserted = result.getRegistros().get(0);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.getValorNum()).isEqualTo(valor);

            verify(goalDAO, never()).updateGoalSatsByGoalId(any(), eq(numMid), any());
        }

        @Order(2)
        @Test
        public void createNumRecord_whenStatsUpdateEmpty_skipsStatsUpdate() {
            LocalDate date = LocalDate.of(2025, 7, 3);
            double valor = 55.5;
            NumRecord record = new NumRecord(valor, date);

            NumGoal result = recordPersistenceController.createNumRecord(record, new Document(), numMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).hasSize(2);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isFalse();
            NumRecord inserted = result.getRegistros().get(1);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.getValorNum()).isEqualTo(valor);

            verify(goalDAO, never()).updateGoalSatsByGoalId(any(), eq(numMid), any());
        }

        @Order(3)
        @Test
        public void createNumRecord_whenValid_insertsRecordAndReturnsGoal() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            double valor = 77.7;
            NumRecord record = new NumRecord(valor, date);
            Document statsUpdate = new Document("valorAlcanzado", true);

            NumGoal result = recordPersistenceController.createNumRecord(record, statsUpdate, numMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getRegistros()).hasSize(3);
            Assertions.assertThat(result.getEstadisticas().getValorAlcanzado()).isTrue();
            NumRecord inserted = result.getRegistros().get(2);
            Assertions.assertThat(inserted.getFecha()).isEqualTo(date);
            Assertions.assertThat(inserted.getValorNum()).isEqualTo(valor);

            numDate = inserted.getFecha().toString();
        }

        @Test
        public void createNumRecord_whenInsertRecordReturnsNull_throwsNotFoundException() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            double valor = 77.7;
            NumRecord record = new NumRecord(valor, date);
            Document statsUpdate = new Document("valorAlcanzado", true);

            doReturn(null)
                    .when(goalDAO).insertRecordInListByGoalId(any(ClientSession.class), eq(numMid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.createNumRecord(record, statsUpdate, numMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta del registro no encontrada");
        }

        @Test
        public void createNumRecord_whenUpdateStatsReturnsNull_throwsNotFoundException() {
            LocalDate date = LocalDate.of(2025, 7, 2);
            double valor = 77.7;
            NumRecord record = new NumRecord(valor, date);
            Document statsUpdate = new Document("valorAlcanzado", true);
            Document dummyGoalDoc = new Document();

            doReturn(dummyGoalDoc)
                    .when(goalDAO).insertRecordInListByGoalId(any(ClientSession.class), eq(numMid), any(Document.class));
            doReturn(null)
                    .when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(numMid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.createNumRecord(record, statsUpdate, numMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta del registro no encontrada");
        }
    }

    @Order(3)
    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        public void delete_whenValidBoolDate_removesBoolRecord() {
            Assertions.assertThatCode(() ->
                    recordPersistenceController.delete(boolMid, boolDate)
            ).doesNotThrowAnyException();

            verify(goalDAO).removeRecordFromListByGoalIdAndDate(
                    any(ClientSession.class), eq(boolMid), eq(boolDate)
            );
        }

        @Test
        public void delete_whenValidNumDate_removesNumRecord() {
            Assertions.assertThatCode(() ->
                    recordPersistenceController.delete(numMid, numDate)
            ).doesNotThrowAnyException();

            verify(goalDAO).removeRecordFromListByGoalIdAndDate(
                    any(ClientSession.class), eq(numMid), eq(numDate)
            );
        }

        @Test
        public void delete_whenRemoveRecordReturnsNull_throwsNotFoundException() {
            doReturn(null)
                    .when(goalDAO).removeRecordFromListByGoalIdAndDate(any(ClientSession.class), eq(boolMid), eq(boolDate));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.delete(boolMid, boolDate)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta o Registro no encontrados");
        }

        @Test
        public void delete_whenDaoThrowsRuntimeException_wrapsAndRethrows() {
            doThrow(new RuntimeException("boom"))
                    .when(goalDAO).removeRecordFromListByGoalIdAndDate(any(ClientSession.class), eq(numMid), eq(numDate));

            Assertions.assertThatThrownBy(() ->
                            recordPersistenceController.delete(numMid, numDate)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al borrar el registro")
                    .hasRootCauseInstanceOf(RuntimeException.class)
                    .getRootCause().hasMessage("boom");
        }
    }
}