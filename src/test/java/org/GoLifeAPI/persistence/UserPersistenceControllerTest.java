package org.GoLifeAPI.persistence;

import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;
import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLifeAPI.exception.ConflictException;
import org.GoLifeAPI.exception.NotFoundException;
import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.infrastructure.MongoService;
import org.GoLifeAPI.mapper.persistence.GoalDocMapper;
import org.GoLifeAPI.mapper.persistence.RecordDocMapper;
import org.GoLifeAPI.mapper.persistence.UserDocMapper;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.persistence.implementation.UserPersistenceController;
import org.GoLifeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLifeAPI.persistence.implementation.dao.UserDAO;
import org.GoLifeAPI.persistence.implementation.transaction.TransactionRunner;
import org.assertj.core.api.Assertions;
import org.bson.BsonDocument;
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
public class UserPersistenceControllerTest {

    private FirebaseService firebaseService;
    private MongoService mongoService;
    private TransactionRunner transactionRunner;
    private UserDocMapper userDocMapper;
    private UserDAO userDAO;
    private GoalDAO goalDAO;

    @InjectMocks
    private UserPersistenceController userPersistenceController;

    private String uid;

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
                    userDocMapper = spy(new UserDocMapper(new GoalDocMapper(new RecordDocMapper())));
                    userDAO = spy(new UserDAO(mongoService));
                    goalDAO = spy(new GoalDAO(mongoService));
                });
        ReflectionTestUtils.setField(userPersistenceController, "transactionRunner", transactionRunner);
        ReflectionTestUtils.setField(userPersistenceController, "userDocMapper", userDocMapper);
        ReflectionTestUtils.setField(userPersistenceController, "userDAO", userDAO);
        ReflectionTestUtils.setField(userPersistenceController, "goalDAO", goalDAO);
        firebaseService = mock(FirebaseService.class);
        when(firebaseService.deleteFirebaseUser(any())).thenReturn(true);
        ReflectionTestUtils.setField(userPersistenceController, "firebaseService", firebaseService);
        uid = "test-uid";
    }

    @AfterAll
    public void nextTestSetUp() {
        User input = new User(uid, "apellidos", "nombre");
        userPersistenceController.create(input, uid);
        MongoContainer.setUid(uid);
    }

    @BeforeEach
    public void beforeEach() {
        doCallRealMethod().when(transactionRunner).run(any());
        doCallRealMethod().when(userDAO).insertDoc(any(ClientSession.class), any(Document.class));
        doCallRealMethod().when(userDAO).findDocById(any(ClientSession.class), any(String.class));
        doCallRealMethod().when(userDAO).findUserByUid(any(String.class));
        doCallRealMethod().when(userDAO).updateUserByUid(any(ClientSession.class), any(String.class), any(Document.class));
        doCallRealMethod().when(userDAO).deleteUserByUid(any(ClientSession.class), any(String.class));

        doCallRealMethod().when(goalDAO).deleteManyGoalsByUid(any(ClientSession.class), any(String.class));

        clearInvocations(firebaseService);
        clearInvocations(userDAO);
        clearInvocations(goalDAO);
    }

    @Order(1)
    @Nested
    @DisplayName("create")
    public class Create {

        @Test
        public void create_whenValid_returnsUser() {
            User input = new User(uid, "apellidos", "nombre");

            User result = userPersistenceController.create(input, uid);

            Assertions.assertThat(result.getUid()).isEqualTo(input.getUid());
            Assertions.assertThat(result.getApellidos()).isEqualTo(input.getApellidos());
            Assertions.assertThat(result.getNombre()).isEqualTo(input.getNombre());
        }

        @Test
        public void create_whenInsertDocReturnsNull_throwsRuntimeExceptionAndDeletesFirebase() {
            User input = new User(uid, "a", "n");
            doReturn(null).when(userDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            userPersistenceController.create(input, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear el usuario");

            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void create_whenInsertDocReturnsBlank_throwsRuntimeExceptionAndDeletesFirebase() {
            User input = new User(uid, "a", "n");
            doReturn("   ").when(userDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            userPersistenceController.create(input, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear el usuario");

            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void create_whenFindDocByIdReturnsNull_throwsRuntimeExceptionAndDeletesFirebase() {
            User input = new User(uid, "a", "n");
            doReturn(null).when(userDAO).findDocById(any(ClientSession.class), any(String.class));

            Assertions.assertThatThrownBy(() ->
                            userPersistenceController.create(input, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear el usuario");

            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void create_whenMongoWriteException_throwsConflictException() {
            User input = new User(uid, "a", "n");
            WriteError writeError = new WriteError(11000, "E11000 duplicate key error", new BsonDocument());
            ServerAddress serverAddress = new ServerAddress("localhost", 27017);
            doThrow(new MongoWriteException(writeError, serverAddress)).when(userDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            userPersistenceController.create(input, uid)
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("El usuario ya existe");

            verify(firebaseService, never()).deleteFirebaseUser(any());
        }

        @Test
        public void create_whenRuntimeExceptionInTransaction_throwsRuntimeExceptionAndDeletesFirebase() {
            User input = new User(uid, "a", "n");
            doThrow(new RuntimeException("boom")).when(userDAO).insertDoc(any(ClientSession.class), any(Document.class));

            Assertions.assertThatThrownBy(() ->
                            userPersistenceController.create(input, uid)
                    ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al crear el usuario");

            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }
    }

    @Order(2)
    @Nested
    @DisplayName("read")
    public class Read {
        @Test
        public void read_whenUserExists_returnsUser() {
            User result = userPersistenceController.read(uid);

            Assertions.assertThat(result.getUid()).isEqualTo(uid);
            Assertions.assertThat(result.getApellidos()).isEqualTo("apellidos");
            Assertions.assertThat(result.getNombre()).isEqualTo("nombre");
            verify(userDAO).findUserByUid(eq(uid));
        }

        @Test
        public void read_whenUserNotFound_throwsNotFoundException() {
            doReturn(null).when(userDAO).findUserByUid(eq(uid));

            Assertions.assertThatThrownBy(() -> userPersistenceController.read(uid))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No se ha encontrado al usuario");
            verify(userDAO).findUserByUid(eq(uid));
        }

        @Test
        public void read_whenRuntimeExceptionInDAO_throwsRuntimeException() {
            doThrow(new RuntimeException("boom")).when(userDAO).findUserByUid(eq(uid));

            Assertions.assertThatThrownBy(() -> userPersistenceController.read(uid))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al leer el usuario");
            verify(userDAO).findUserByUid(eq(uid));
        }

    }

    @Order(3)
    @Nested
    @DisplayName("update")
    public class Update {
        @Test
        public void update_whenUserExists_returnsUser() {
            Document update = new Document("nombre", "nuevo")
                    .append("apellidos", "apellido");

            User result = userPersistenceController.update(update, uid);

            Assertions.assertThat(result.getUid()).isEqualTo(uid);
            Assertions.assertThat(result.getApellidos()).isEqualTo("apellido");
            Assertions.assertThat(result.getNombre()).isEqualTo("nuevo");
            verify(userDAO).updateUserByUid(any(ClientSession.class), eq(uid), eq(update));
        }

        @Test
        public void update_whenUserNotFound_throwsNotFoundException() {
            Document update = new Document("nombre", "x");
            doReturn(null)
                    .when(userDAO).updateUserByUid(any(ClientSession.class), eq(uid), eq(update));

            Assertions.assertThatThrownBy(() -> userPersistenceController.update(update, uid))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario no encontrado");

            verify(userDAO).updateUserByUid(any(ClientSession.class), eq(uid), eq(update));
        }

        @Test
        public void update_whenRuntimeExceptionInDAO_throwsRuntimeException() {
            Document update = new Document("foo", "bar");
            doThrow(new RuntimeException("boom"))
                    .when(userDAO).updateUserByUid(any(ClientSession.class), eq(uid), eq(update));

            Assertions.assertThatThrownBy(() -> userPersistenceController.update(update, uid))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al editar el usuario");

            verify(userDAO).updateUserByUid(any(ClientSession.class), eq(uid), eq(update));
        }
    }

    @Order(4)
    @Nested
    @DisplayName("delete")
    public class Delete {
        @Test
        public void delete_whenAllOperationsSucceed_noException() {
            when(firebaseService.deleteFirebaseUser(any())).thenReturn(true);

            Assertions.assertThatCode(() -> userPersistenceController.delete(uid))
                    .doesNotThrowAnyException();

            verify(userDAO).deleteUserByUid(any(ClientSession.class), eq(uid));
            verify(goalDAO).deleteManyGoalsByUid(any(ClientSession.class), eq(uid));
            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void delete_whenUserDeleteNotAcknowledged_throwsRuntimeExceptionAndNoFirebaseDelete() {
            doReturn(DeleteResult.unacknowledged())
                    .when(userDAO).deleteUserByUid(any(ClientSession.class), eq(uid));

            Assertions.assertThatThrownBy(() -> userPersistenceController.delete(uid))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al borrar el usuario");

            verify(firebaseService, never()).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void delete_whenUserDeleteCountZero_throwsNotFoundExceptionAndNoFirebaseDelete() {
            doReturn(DeleteResult.acknowledged(0))
                    .when(userDAO).deleteUserByUid(any(ClientSession.class), eq(uid));

            Assertions.assertThatThrownBy(() -> userPersistenceController.delete(uid))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuario no encontrado");

            verify(firebaseService, never()).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void delete_whenGoalDeleteNotAcknowledged_throwsRuntimeExceptionAndNoFirebaseDelete() {
            doReturn(DeleteResult.acknowledged(1))
                    .when(userDAO).deleteUserByUid(any(ClientSession.class), eq(uid));
            doReturn(DeleteResult.unacknowledged())
                    .when(goalDAO).deleteManyGoalsByUid(any(ClientSession.class), eq(uid));

            Assertions.assertThatThrownBy(() -> userPersistenceController.delete(uid))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al borrar el usuario");

            verify(firebaseService, never()).deleteFirebaseUser(eq(uid));
        }

        @Test
        public void delete_whenFirebaseDeleteReturnsFalse_throwsRuntimeException() {
            doReturn(DeleteResult.acknowledged(1))
                    .when(userDAO).deleteUserByUid(any(ClientSession.class), eq(uid));
            doReturn(DeleteResult.acknowledged(5))
                    .when(goalDAO).deleteManyGoalsByUid(any(ClientSession.class), eq(uid));
            when(firebaseService.deleteFirebaseUser(eq(uid))).thenReturn(false);

            Assertions.assertThatThrownBy(() -> userPersistenceController.delete(uid))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error interno al borrar el usuario");

            verify(firebaseService).deleteFirebaseUser(eq(uid));
        }
    }
}