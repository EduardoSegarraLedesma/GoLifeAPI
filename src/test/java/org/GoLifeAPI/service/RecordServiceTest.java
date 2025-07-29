package org.GoLifeAPI.service;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.service.GoalDtoMapper;
import org.GoLIfeAPI.mapper.service.RecordDtoMapper;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.GoalStats;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.GoLIfeAPI.persistence.interfaces.IRecordPersistenceController;
import org.GoLIfeAPI.service.implementation.GoalService;
import org.GoLIfeAPI.service.implementation.RecordService;
import org.GoLIfeAPI.service.implementation.StatsService;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecordServiceTest {

    @Spy
    private RecordDtoMapper recordDtoMapper = new RecordDtoMapper();
    @Spy
    private GoalDtoMapper goalDtoMapper = new GoalDtoMapper(recordDtoMapper);
    @Spy
    private StatsService statsService = new StatsService();

    @Mock
    GoalService goalService;
    @Mock
    private IRecordPersistenceController recordPersistenceController;

    @InjectMocks
    private RecordService recordService;

    private String uid;
    private String mid;
    private CreateBoolRecordDTO createBoolRecordDTO;
    private CreateNumRecordDTO createNumRecordDTO;
    private BoolGoal boolGoal;
    private NumGoal numGoal;

    @BeforeEach
    void setUp() {
        clearInvocations(recordPersistenceController);
        clearInvocations(goalService);
        uid = "test-uid";
        mid = "test-mid";
        createBoolRecordDTO = null;
        createNumRecordDTO = null;
        boolGoal = null;
        numGoal = null;
    }

    @Nested
    @DisplayName("createBoolRecord")
    class CreateBoolRecord {
        @Test
        public void createBoolRecord_whenValid_delegatesToPersistenceAndReturnsDTO() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            createBoolRecordDTO = new CreateBoolRecordDTO(true, date);

            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "nombre", "desc",
                    date, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, date), new ArrayList<>());
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);

            when(recordPersistenceController.createBoolrecord(any(BoolRecord.class), any(Document.class), eq(mid)))
                    .thenAnswer(invocation -> {
                        boolGoal.getRegistros().add(new BoolRecord(true, date));
                        return boolGoal;
                    });

            ResponseBoolGoalDTO result = recordService.createBoolRecord(createBoolRecordDTO, uid, mid);

            Assertions.assertThat(result.getRegistros()).hasSize(1);
            Assertions.assertThat(result.getRegistros().get(0).getFecha()).isEqualTo(date);

            verify(recordPersistenceController).createBoolrecord(any(BoolRecord.class), any(), eq(mid));
        }

        @Test
        public void createBoolRecord_whenGoalIsNum_throwsBadRequestException() {
            numGoal = new NumGoal(LocalDate.of(2025, 7, 1));
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(numGoal);
            createBoolRecordDTO = new CreateBoolRecordDTO(true, LocalDate.of(2025, 7, 1));

            Assertions.assertThatThrownBy(() ->
                            recordService.createBoolRecord(createBoolRecordDTO, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("Tipo de registro incorrecto para la meta");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createBoolRecord_whenDateBeforeGoal_throwsBadRequestException() {
            LocalDate start = LocalDate.of(2025, 7, 2);
            LocalDate recordDate = LocalDate.of(2025, 7, 1);
            createBoolRecordDTO = new CreateBoolRecordDTO(true, recordDate);

            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.createBoolRecord(createBoolRecordDTO, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("La fecha del registro no puede ser anterior a la fecha inicial de la meta");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createBoolRecord_whenDuplicateDate_throwsConflictException() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            createBoolRecordDTO = new CreateBoolRecordDTO(true, date);

            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    date, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, date), new ArrayList<>());
            boolGoal.getRegistros().add(new BoolRecord(true, date));
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.createBoolRecord(createBoolRecordDTO, uid, mid)
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("Ya existe un registro en esa fecha");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createBoolRecord_whenUnexpectedGoalType_throwsIllegalStateException() {
            createBoolRecordDTO = new CreateBoolRecordDTO(true, LocalDate.of(2025, 7, 1));
            Goal unknown = mock(Goal.class);
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(unknown);

            Assertions.assertThatThrownBy(() ->
                            recordService.createBoolRecord(createBoolRecordDTO, uid, mid)
                    ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tipo de meta inesperada");

            verifyNoInteractions(recordPersistenceController);
        }
    }

    @Nested
    @DisplayName("createNumRecord")
    class CreateNumRecord {
        @Test
        public void createNumRecord_whenValid_delegatesToPersistenceAndReturnsDTO() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            createNumRecordDTO = new CreateNumRecordDTO(42.0, date);
            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "nombre", "desc",
                    date, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, date), new ArrayList<>(),
                    100.0, "km");
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(numGoal);

            when(recordPersistenceController.createNumRecord(
                    any(NumRecord.class),
                    any(Document.class),
                    eq(mid)))
                    .thenAnswer(invocation -> {
                        numGoal.getRegistros().add(new NumRecord(42.0, date));
                        return numGoal;
                    });

            ResponseNumGoalDTO result = recordService.createNumRecord(createNumRecordDTO, uid, mid);

            Assertions.assertThat(result.getRegistros()).hasSize(1);
            Assertions.assertThat(result.getRegistros().get(0).getFecha()).isEqualTo(date);

            verify(recordPersistenceController).createNumRecord(any(NumRecord.class), any(), eq(mid));
        }

        @Test
        public void createNumRecord_whenGoalIsBool_throwsBadRequestException() {
            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);
            createNumRecordDTO = new CreateNumRecordDTO(42.0, LocalDate.of(2025, 7, 1));

            Assertions.assertThatThrownBy(() ->
                            recordService.createNumRecord(createNumRecordDTO, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("Tipo de registro incorrecto para la meta");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createNumRecord_whenDateBeforeGoal_throwsBadRequestException() {
            LocalDate start = LocalDate.of(2025, 7, 2);
            LocalDate recordDate = LocalDate.of(2025, 7, 1);
            createNumRecordDTO = new CreateNumRecordDTO(42.0, recordDate);

            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "n", "d",
                    start, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>(),
                    100.0, "km");
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(numGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.createNumRecord(createNumRecordDTO, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("La fecha del registro no puede ser anterior a la fecha inicial de la meta");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createNumRecord_whenDuplicateDate_throwsConflictException() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            createNumRecordDTO = new CreateNumRecordDTO(42.0, date);

            List<NumRecord> registros = new ArrayList<>();
            registros.add(new NumRecord(42.0, date));
            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "n", "d",
                    date, false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, date), registros,
                    100.0, "km");
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(numGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.createNumRecord(createNumRecordDTO, uid, mid)
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("Ya existe un registro en esa fecha");

            verifyNoInteractions(recordPersistenceController);
        }

        @Test
        public void createNumRecord_whenUnexpectedGoalType_throwsIllegalStateException() {
            createNumRecordDTO = new CreateNumRecordDTO(42.0, LocalDate.of(2025, 7, 1));
            Goal unknown = mock(Goal.class);
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(unknown);

            Assertions.assertThatThrownBy(() ->
                            recordService.createNumRecord(createNumRecordDTO, uid, mid)
                    ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tipo de meta inesperada");

            verifyNoInteractions(recordPersistenceController);
        }
    }

    @Nested
    @DisplayName("deleteRecord")
    class DeleteRecord {
        @Test
        public void deleteRecord_whenRecordExists_delegatesToPersistence() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            BoolRecord record = new BoolRecord(true, date);
            boolGoal = new BoolGoal(null, null, null
                    , null, null, 1
                    , Enums.Duracion.Dias, null, null);
            boolGoal.addRegistro(record);

            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);

            recordService.deleteRecord(uid, mid, date);

            verify(recordPersistenceController).delete(eq(mid), eq(date.toString()));
        }

        @Test
        public void deleteRecord_whenBoolRecordNotExists_throwsNotFoundException() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            BoolGoal boolGoal = new BoolGoal(null, null, null, null,
                    null, null, 1, Enums.Duracion.Dias,
                    new GoalStats(null, null), new ArrayList<>());
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(boolGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.deleteRecord(uid, mid, date)
                    ).isInstanceOf(NotFoundException.class)
                    .hasMessage("No existe un registro en esa fecha");

            verify(recordPersistenceController, never()).delete(anyString(), anyString());
        }

        @Test
        public void deleteRecord_whenGoalTypeUnexpected_throwsIllegalStateException() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            Goal badGoal = mock(Goal.class);
            when(goalService.validateAndGetGoal(uid, mid)).thenReturn(badGoal);

            Assertions.assertThatThrownBy(() ->
                            recordService.deleteRecord(uid, mid, date)
                    ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tipo de meta inesperada");

            verify(recordPersistenceController, never()).delete(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("validateRecord")
    class ValidateRecord {
        @Test
        public void validateRecord_whenRecordBeforeGoalDate_throwsBadRequestException() {
            LocalDate goalDate = LocalDate.of(2025, 7, 2);
            BoolRecord record = new BoolRecord(false, LocalDate.of(2025, 7, 1));
            List<BoolRecord> existing = Collections.emptyList();
            Assertions.assertThatThrownBy(() ->
                            ReflectionTestUtils.invokeMethod(
                                    recordService, "validateRecord",
                                    goalDate, record, existing
                            )
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("La fecha del registro no puede ser anterior a la fecha inicial de la meta");
        }

        @Test
        public void validateRecord_whenRecordOnOrAfterGoalDate_noException() {
            LocalDate goalDate = LocalDate.of(2025, 7, 1);
            BoolRecord record = new BoolRecord(false, LocalDate.of(2025, 7, 1));
            List<BoolRecord> existing = Collections.emptyList();
            Assertions.assertThatCode(() ->
                    ReflectionTestUtils.invokeMethod(
                            recordService, "validateRecord",
                            goalDate, record, existing
                    )
            ).doesNotThrowAnyException();
        }

        @Test
        public void validateRecord_whenDuplicateDate_throwsConflictException() {
            LocalDate goalDate = LocalDate.of(2025, 7, 1);
            BoolRecord record = new BoolRecord(true, goalDate);
            List<BoolRecord> existing = List.of(new BoolRecord(false, goalDate));
            Assertions.assertThatThrownBy(() ->
                            ReflectionTestUtils.invokeMethod(
                                    recordService, "validateRecord",
                                    goalDate, record, existing
                            )
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("Ya existe un registro en esa fecha");
        }
    }
}