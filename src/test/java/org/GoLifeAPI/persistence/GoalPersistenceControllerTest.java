package org.GoLifeAPI.persistence;

import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.GoLIfeAPI.mapper.persistence.GoalDocMapper;
import org.GoLIfeAPI.mapper.persistence.RecordDocMapper;
import org.GoLIfeAPI.mapper.persistence.UserDocMapper;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.goal.PartialGoal;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.GoLIfeAPI.persistence.implementation.GoalPersistenceController;
import org.GoLIfeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLIfeAPI.persistence.implementation.dao.UserDAO;
import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
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
public class GoalPersistenceControllerTest {

    private MongoService mongoService;
    private TransactionRunner transactionRunner;
    private UserDocMapper userDocMapper;
    private GoalDocMapper goalDocMapper;
    private UserDAO userDAO;
    private GoalDAO goalDAO;

    @InjectMocks
    private GoalPersistenceController goalPersistenceController;

    String uid;
    String boolMid;
    String numMid;

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
                    goalDocMapper = spy(new GoalDocMapper(new RecordDocMapper()));
                    userDocMapper = spy(new UserDocMapper(goalDocMapper));
                    userDAO = spy(new UserDAO(mongoService));
                    goalDAO = spy(new GoalDAO(mongoService));
                });
        ReflectionTestUtils.setField(goalPersistenceController, "transactionRunner", transactionRunner);
        ReflectionTestUtils.setField(goalPersistenceController, "userDocMapper", userDocMapper);
        ReflectionTestUtils.setField(goalPersistenceController, "goalDocMapper", goalDocMapper);
        ReflectionTestUtils.setField(goalPersistenceController, "userDAO", userDAO);
        ReflectionTestUtils.setField(goalPersistenceController, "goalDAO", goalDAO);
        uid = MongoContainer.getUid();
    }

    @AfterAll
    public void nextTestsSetUp() {
        LocalDate start = LocalDate.of(2025, 7, 1);
        Document statsUpdate = new Document()
                .append("totalMetas", 1)
                .append("totalMetasFinalizadas", 0);

        BoolGoal boolGoal = new BoolGoal(
                uid, "miMeta", "descMeta", start,
                false, 5, Enums.Duracion.Dias,
                false, start
        );

        NumGoal numGoal = new NumGoal(
                uid, "miMetaNum", "descMetaNum",
                start, false, 5, Enums.Duracion.Dias,
                false, start, 42.0, "km"
        );

        User updatedUser = goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid);
        boolMid = updatedUser.getMetas().get(0).get_id().toString();

        updatedUser = goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid);
        numMid = updatedUser.getMetas().get(1).get_id().toString();

        MongoContainer.setBoolMid(boolMid);
        MongoContainer.setNumMid(numMid);
    }

    @BeforeEach
    public void beforeEach() {
        doCallRealMethod().when(transactionRunner).run(any());

        doCallRealMethod().when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
        doCallRealMethod().when(goalDAO).findDocById(any(String.class));
        doCallRealMethod().when(goalDAO).updateGoalByGoalId(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(goalDAO).deleteGoalByGoalId(any(ClientSession.class), any(String.class));

        doCallRealMethod().when(userDAO).findUserByUid(any(String.class));
        doCallRealMethod().when(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(userDAO).updateUserStatsByUid(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(userDAO).updatePartialGoalInListByUidAndGoalId(any(ClientSession.class), any(String.class), any(String.class), any(Document.class));
        doCallRealMethod().when(userDAO).removePartialGoalFromListByUidAndGoalId(any(ClientSession.class), any(String.class), any(String.class));

        clearInvocations(userDAO);
        clearInvocations(goalDAO);
    }

    @Order(1)
    @Nested
    @DisplayName("createBoolGoal")
    class CreateBoolGoal {
        @Test
        public void createBoolGoal_whenValid_insertsGoalAndReturnsUpdatedUser() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(
                    uid, "miMeta", "descMeta", start,
                    false, 5, Enums.Duracion.Dias,
                    false, start
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            User updatedUser = goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid);

            Assertions.assertThat(updatedUser).isNotNull();
            Assertions.assertThat(updatedUser.getMetas()).hasSize(1);

            boolMid = updatedUser.getMetas().get(0).get_id().toString();
        }

        @Test
        public void createBoolGoal_whenInsertDocReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(
                    uid, "miMeta", "descMeta", start,
                    false, 5, Enums.Duracion.Dias,
                    false, start
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn(null)
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
        }

        @Test
        public void createBoolGoal_whenInsertDocReturnsBlank_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(
                    uid, "miMeta", "descMeta", start,
                    false, 5, Enums.Duracion.Dias,
                    false, start
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn("   ")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
        }

        @Test
        public void createBoolGoal_whenInsertPartialGoalReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(
                    uid, "miMeta", "descMeta", start,
                    false, 5, Enums.Duracion.Dias,
                    false, start
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);


            doReturn("64b8f4a2e1c2f90abcd12345")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));

            doReturn(null)
                    .when(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            verify(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
        }

        @Test
        public void createBoolGoal_whenUpdateUserStatsReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(
                    uid, "miMeta", "descMeta", start,
                    false, 5, Enums.Duracion.Dias,
                    false, start
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn("64b8f4a2e1c2f90abcd12345")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            doReturn(new Document("_id", new ObjectId("64b8f4a2e1c2f90abcd12345")))
                    .when(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
            doReturn(null)
                    .when(userDAO).updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createBoolGoal(boolGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            verify(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
            verify(userDAO).updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));
        }
    }

    @Order(2)
    @Nested
    @DisplayName("createNumGoal")
    class CreateNumGoal {
        @Test
        public void createNumGoal_whenValid_insertsGoalAndReturnsUpdatedUser() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal numGoal = new NumGoal(
                    uid, "miMetaNum", "descMetaNum",
                    start, false, 5, Enums.Duracion.Dias,
                    false, start, 42.0, "km"
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            User updatedUser = goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid);

            Assertions.assertThat(updatedUser).isNotNull();
            Assertions.assertThat(updatedUser.getMetas()).hasSize(2);

            numMid = updatedUser.getMetas().get(1).get_id().toString();
        }

        @Test
        public void createNumGoal_whenInsertDocReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal numGoal = new NumGoal(
                    uid, "nombre", "descripcion",
                    start, false, 5, Enums.Duracion.Dias,
                    false, start, 10.0, "unit"
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn(null)
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
        }

        @Test
        public void createNumGoal_whenInsertDocReturnsBlank_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal numGoal = new NumGoal(
                    uid, "nombre", "descripcion",
                    start, false, 5, Enums.Duracion.Dias,
                    false, start, 10.0, "unit"
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn("   ")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
        }

        @Test
        public void createNumGoal_whenInsertPartialGoalReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal numGoal = new NumGoal(
                    uid, "nombre", "descripcion",
                    start, false, 5, Enums.Duracion.Dias,
                    false, start, 10.0, "unit"
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn("64b8f4a2e1c2f90abcd12345")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            doReturn(null)
                    .when(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            verify(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
        }

        @Test
        public void createNumGoal_whenUpdateUserStatsReturnsNull_throwsRuntimeException() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal numGoal = new NumGoal(
                    uid, "nombre", "descripcion",
                    start, false, 5, Enums.Duracion.Dias,
                    false, start, 10.0, "unit"
            );
            Document statsUpdate = new Document()
                    .append("totalMetas", 1)
                    .append("totalMetasFinalizadas", 0);

            doReturn("64b8f4a2e1c2f90abcd12345")
                    .when(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            doReturn(new Document("_id", new ObjectId("64b8f4a2e1c2f90abcd12345")))
                    .when(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
            doReturn(null)
                    .when(userDAO).updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.createNumGoal(numGoal, statsUpdate, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear la meta");

            verify(goalDAO).insertDoc(any(ClientSession.class), any(Document.class));
            verify(userDAO).insertPartialGoalInListByUid(any(ClientSession.class), eq(uid), any(Document.class));
            verify(userDAO).updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));
        }
    }

    @Order(3)
    @Nested
    @DisplayName("read")
    class Read {
        @Test
        public void read_whenBoolGoalExists_returnsBoolGoal() {
            Goal result = goalPersistenceController.read(boolMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(BoolGoal.class);
            BoolGoal actual = (BoolGoal) result;
            Assertions.assertThat(actual.getUid()).isEqualTo(uid);
        }

        @Test
        public void read_whenNumGoalExists_returnsNumGoal() {
            Goal result = goalPersistenceController.read(numMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(NumGoal.class);
            NumGoal actual = (NumGoal) result;
            Assertions.assertThat(actual.getUid()).isEqualTo(uid);
        }

        @Test
        public void read_whenNotFound_throwsNotFoundException() {
            String id = new ObjectId().toHexString();
            doReturn(null).when(goalDAO).findDocById(eq(id));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.read(id)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta no encontrada");
        }

        @Test
        public void read_whenDAOThrowsRuntimeException_throwsRuntimeException() {
            String id = new ObjectId().toHexString();
            doThrow(new RuntimeException("boom")).when(goalDAO).findDocById(eq(id));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.read(id)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al leer la meta");
        }
    }

    @Order(4)
    @Nested
    @DisplayName("updateWithGoalStats")
    class UpdateWithGoalStats {

        @Test
        public void updateWithGoalStats_whenValidBool_updatesFechaFin() {
            Document update = new Document("fecha", "2025-12-31")
                    .append("nombre", "nuevoNombre");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");

            User updated = goalPersistenceController.updateWithGoalStats(
                    update, update, statsUpdate, uid, boolMid
            );

            Assertions.assertThat(updated).isNotNull();
            PartialGoal updatedGoal = updated.getMetas().get(0);
            Assertions.assertThat(updatedGoal.getNombre()).isEqualTo("nuevoNombre");
            Assertions.assertThat(updatedGoal.getFecha()).isEqualTo(LocalDate.parse("2025-12-31"));
        }

        @Test
        public void updateWithGoalStats_whenValidNum_updatesFechaFin() {
            Document update = new Document("fecha", "2025-12-31")
                    .append("nombre", "nuevoNombre");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");

            User updated = goalPersistenceController.updateWithGoalStats(
                    update, update, statsUpdate, uid, numMid
            );

            Assertions.assertThat(updated).isNotNull();
            PartialGoal updatedGoal = updated.getMetas().get(1);
            Assertions.assertThat(updatedGoal.getNombre()).isEqualTo("nuevoNombre");
            Assertions.assertThat(updatedGoal.getFecha()).isEqualTo(LocalDate.parse("2025-12-31"));
        }

        @Test
        public void updateWithGoalStats_whenStatsUpdateEmpty_skipsStatsUpdate() {
            Document update = new Document("nombre", "soloNombre");
            Document emptyStats = new Document();
            doThrow(new AssertionError("No debería invocar a updateGoalSatsByGoalId"))
                    .when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));

            User updated = goalPersistenceController.updateWithGoalStats(
                    update, update, emptyStats, uid, boolMid
            );

            Assertions.assertThat(updated).isNotNull();
            Assertions.assertThat(updated.getMetas().get(0).getNombre()).isEqualTo("soloNombre");

            verify(goalDAO, never()).updateGoalSatsByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));
        }

        @Test
        public void updateWithGoalStats_whenGoalNotFound_throwsNotFoundException() {
            Document update = new Document("nombre", "X");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");

            doReturn(null).when(goalDAO).updateGoalByGoalId(any(ClientSession.class), eq(boolMid), eq(update));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithGoalStats(update, update, statsUpdate, uid, boolMid)
                    )
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta no encontrada");
        }

        @Test
        public void updateWithGoalStats_whenStatsUpdateNonEmptyAndDaoReturnsNull_throwsNotFoundException() {
            Document update = new Document("nombre", "cualquier");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");

            doReturn(new Document())
                    .when(goalDAO).updateGoalByGoalId(any(ClientSession.class), eq(boolMid), eq(update));
            doReturn(null)
                    .when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(boolMid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithGoalStats(update, update, statsUpdate, uid, boolMid)
                    )
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }

        @Test
        public void updateWithGoalStats_whenStatsUpdateFails_throwsNotFoundException() {
            Document update = new Document("nombre", "X");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");
            Document dummyDoc = new Document("_id", new ObjectId());

            doReturn(dummyDoc).when(goalDAO).updateGoalByGoalId(any(ClientSession.class), eq(boolMid), eq(update));
            doReturn(null).when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(boolMid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithGoalStats(update, update, statsUpdate, uid, boolMid)
                    )
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }

        @Test
        public void updateWithGoalStats_whenPartialUpdateFails_throwsNotFoundException() {
            Document update = new Document("nombre", "X");
            Document statsUpdate = new Document("fechaFin", "2025-12-31");
            Document dummyDoc = new Document("_id", new ObjectId());

            doReturn(dummyDoc).when(goalDAO).updateGoalByGoalId(any(ClientSession.class), eq(numMid), eq(update));
            doReturn(dummyDoc).when(goalDAO).updateGoalSatsByGoalId(any(ClientSession.class), eq(numMid), eq(statsUpdate));
            doReturn(null).when(userDAO).updatePartialGoalInListByUidAndGoalId(any(ClientSession.class), eq(uid), eq(numMid), eq(update));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithGoalStats(update, update, statsUpdate, uid, numMid)
                    )
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }
    }

    @Order(5)
    @Nested
    @DisplayName("updateWithUserStats")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateWithUserStats {
        @Order(1)
        @Test
        public void updateWithUserStats_whenValidBool_setsFinalizadoTrue() {
            Document update = new Document("finalizado", true);
            Document statsUpdate = new Document("totalMetas", 0).append("totalMetasFinalizadas", 1);

            User updated = goalPersistenceController.updateWithUserStats(
                    update, update, statsUpdate, uid, boolMid
            );

            Assertions.assertThat(updated).isNotNull();
            Assertions.assertThat(updated.getMetas().get(0).getFinalizado()).isTrue();
            Assertions.assertThat(updated.getEstadisticas().getTotalMetasFinalizadas()).isEqualTo(1);
        }

        @Order(2)
        @Test
        public void updateWithUserStats_whenValidNum_setsFinalizadoTrue() {
            Document update = new Document("finalizado", true);
            Document statsUpdate = new Document("totalMetas", 0).append("totalMetasFinalizadas", 1);

            User updated = goalPersistenceController.updateWithUserStats(
                    update, update, statsUpdate, uid, numMid
            );

            Assertions.assertThat(updated).isNotNull();
            Assertions.assertThat(updated.getMetas().get(1).getFinalizado()).isTrue();
            Assertions.assertThat(updated.getEstadisticas().getTotalMetasFinalizadas()).isEqualTo(2);
        }

        @Test
        public void updateWithUserStats_whenGoalNotFound_throwsNotFoundException() {
            Document update = new Document("finalizado", true);
            Document statsUpdate = new Document("totalMetas", 0);

            doReturn(null).when(goalDAO).updateGoalByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithUserStats(update, update, statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta no encontrada");
        }

        @Test
        public void updateWithUserStats_whenPartialUpdateFails_throwsNotFoundException() {
            Document update = new Document("finalizado", true);
            Document statsUpdate = new Document("totalMetas", 0);

            doReturn(new Document("_id", new ObjectId(boolMid))).when(goalDAO)
                    .updateGoalByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));
            doReturn(null).when(userDAO)
                    .updatePartialGoalInListByUidAndGoalId(any(ClientSession.class), eq(uid), eq(boolMid), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithUserStats(update, update, statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }

        @Test
        public void updateWithUserStats_whenUserStatsFails_throwsNotFoundException() {
            Document update = new Document("finalizado", true);
            Document statsUpdate = new Document("totalMetas", 0);

            doReturn(new Document("_id", new ObjectId(boolMid))).when(goalDAO)
                    .updateGoalByGoalId(any(ClientSession.class), eq(boolMid), any(Document.class));
            doReturn(new Document("_id", new ObjectId(boolMid))).when(userDAO)
                    .updatePartialGoalInListByUidAndGoalId(any(ClientSession.class), eq(uid), eq(boolMid), any(Document.class));
            doReturn(null).when(userDAO)
                    .updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.updateWithUserStats(update, update, statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }
    }

    @Order(6)
    @Nested
    @DisplayName("delete")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Delete {

        @Order(1)
        @Test
        public void deleteBoolGoal_whenExists_removesAndReturnsUserStats() {
            Document statsUpdate = new Document()
                    .append("totalMetas", -1)
                    .append("totalMetasFinalizadas", 0);

            UserStats result = goalPersistenceController.delete(statsUpdate, uid, boolMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getTotalMetas()).isEqualTo(1);
        }

        @Order(2)
        @Test
        public void deleteNumGoal_whenExists_removesAndReturnsUserStats() {
            Document statsUpdate = new Document()
                    .append("totalMetas", -1)
                    .append("totalMetasFinalizadas", 0);

            UserStats result = goalPersistenceController.delete(statsUpdate, uid, numMid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getTotalMetas()).isEqualTo(0);
        }

        @Test
        public void delete_whenDeleteGoalNotAcknowledged_throwsRuntimeException() {
            Document statsUpdate = new Document();
            DeleteResult nr = mock(DeleteResult.class);
            doReturn(false).when(nr).wasAcknowledged();
            doReturn(nr).when(goalDAO).deleteGoalByGoalId(any(ClientSession.class), eq(boolMid));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.delete(statsUpdate, uid, boolMid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al eliminar la meta");
        }

        @Test
        public void delete_whenDeleteGoalCountZero_throwsNotFoundException() {
            Document statsUpdate = new Document();
            DeleteResult nr = mock(DeleteResult.class);
            doReturn(true).when(nr).wasAcknowledged();
            doReturn(0L).when(nr).getDeletedCount();
            doReturn(nr).when(goalDAO).deleteGoalByGoalId(any(ClientSession.class), eq(boolMid));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.delete(statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Meta no encontrada");
        }

        @Test
        public void delete_whenRemovePartialGoalReturnsNull_throwsNotFoundException() {
            Document statsUpdate = new Document();
            DeleteResult nr = mock(DeleteResult.class);
            doReturn(true).when(nr).wasAcknowledged();
            doReturn(1L).when(nr).getDeletedCount();
            doReturn(nr).when(goalDAO).deleteGoalByGoalId(any(ClientSession.class), eq(boolMid));
            doReturn(null).when(userDAO).removePartialGoalFromListByUidAndGoalId(any(ClientSession.class), eq(uid), eq(boolMid));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.delete(statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }

        @Test
        public void delete_whenUpdateUserStatsReturnsNull_throwsNotFoundException() {
            Document statsUpdate = new Document();
            DeleteResult nr = mock(DeleteResult.class);
            doReturn(true).when(nr).wasAcknowledged();
            doReturn(1L).when(nr).getDeletedCount();
            doReturn(nr).when(goalDAO).deleteGoalByGoalId(any(ClientSession.class), eq(boolMid));
            doReturn(new Document()).when(userDAO).removePartialGoalFromListByUidAndGoalId(any(ClientSession.class), eq(uid), eq(boolMid));
            doReturn(null).when(userDAO).updateUserStatsByUid(any(ClientSession.class), eq(uid), eq(statsUpdate));

            Assertions.assertThatThrownBy(() ->
                            goalPersistenceController.delete(statsUpdate, uid, boolMid)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario de la Meta no encontrado");
        }
    }
}