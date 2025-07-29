package org.GoLifeAPI.service;

import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.mapper.service.GoalDtoMapper;
import org.GoLIfeAPI.mapper.service.GoalPatchMapper;
import org.GoLIfeAPI.mapper.service.RecordDtoMapper;
import org.GoLIfeAPI.mapper.service.UserDtoMapper;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.GoalStats;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.GoLIfeAPI.persistence.interfaces.IGoalPersistenceController;
import org.GoLIfeAPI.service.implementation.GoalService;
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

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Spy
    private GoalDtoMapper goalDtoMapper = new GoalDtoMapper(new RecordDtoMapper());
    @Spy
    private UserDtoMapper userDtoMapper = new UserDtoMapper(goalDtoMapper);
    @Spy
    private GoalPatchMapper goalPatchMapper = new GoalPatchMapper();
    @Spy
    private StatsService statsService = new StatsService();

    @Mock
    private IGoalPersistenceController goalPersistenceController;

    @InjectMocks
    private GoalService goalService;

    private String uid;
    private String mid;

    @BeforeEach
    void setUp() {
        clearInvocations(goalPersistenceController);
        clearInvocations(statsService);
        uid = "test-uid";
        mid = "test-mid";
    }

    @Nested
    @DisplayName("createBoolGoal")
    class CreateBoolGoal {
        @Test
        public void createBoolGoal_whenValid_delegatesToPersistenceAndReturnsDTO() {
            CreateBoolGoalDTO dto = new CreateBoolGoalDTO(
                    "actividad", "desc",
                    LocalDate.of(2025, 7, 1),
                    7, Enums.Duracion.Dias
            );

            User user = new User(uid, "apellidos", "nombre");
            when(goalPersistenceController.createBoolGoal(
                    any(BoolGoal.class), any(Document.class), eq(uid)
            )).thenReturn(user);

            ResponseUserDTO result = goalService.createBoolGoal(dto, uid);

            Assertions.assertThat(result).isNotNull();
            verify(statsService).calculateFinalGoalDate(
                    dto.getFecha(), dto.getDuracionValor(), dto.getDuracionUnidad()
            );
            verify(goalPersistenceController).createBoolGoal(
                    any(BoolGoal.class), any(Document.class), eq(uid)
            );
            verify(statsService).getUserStatsUpdateDoc(1, 0);
            verify(userDtoMapper).mapUserToResponseUserDTO(user);
        }

    }

    @Nested
    @DisplayName("createNumGoal")
    class CreateNumGoal {
        @Test
        public void createNumGoal_whenValid_delegatesToPersistenceAndReturnsDTO() {
            CreateNumGoalDTO dto = new CreateNumGoalDTO(
                    "actividad", "desc",
                    LocalDate.of(2025, 7, 1),
                    7, Enums.Duracion.Dias,
                    123.45, "km"
            );

            User user = new User(uid, "apellidos", "nombre");
            when(goalPersistenceController.createNumGoal(
                    any(NumGoal.class), any(Document.class), eq(uid)
            )).thenReturn(user);

            ResponseUserDTO result = goalService.createNumGoal(dto, uid);

            Assertions.assertThat(result).isNotNull();
            verify(statsService).calculateFinalGoalDate(
                    dto.getFecha(), dto.getDuracionValor(), dto.getDuracionUnidad()
            );
            verify(statsService).getUserStatsUpdateDoc(1, 0);
            verify(goalPersistenceController).createNumGoal(
                    any(NumGoal.class), any(Document.class), eq(uid)
            );
            verify(userDtoMapper).mapUserToResponseUserDTO(user);
        }
    }

    @Nested
    @DisplayName("getGoal")
    class GetGoal {
        @Test
        public void getGoal_whenNumGoal_returnsDTOWithCorrectFields() {
            LocalDate date = LocalDate.of(2025, 7, 1);
            NumGoal goal = new NumGoal(uid, new ObjectId(), "name", "desc",
                    date, true, 5, Enums.Duracion.Semanas,
                    new GoalStats(true, date), new ArrayList<>(), 123.45, "unit");
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            ResponseNumGoalDTO result = (ResponseNumGoalDTO) goalService.getGoal(uid, mid);

            Assertions.assertThat(result).isNotNull();

            verify(goalPersistenceController).read(eq(mid));
        }

        @Test
        public void getGoal_whenBoolGoal_returnsDTOWithCorrectFields() {
            LocalDate date = LocalDate.of(2025, 6, 15);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "boolName", "boolDesc",
                    date, false, 10, Enums.Duracion.Meses,
                    new GoalStats(false, date), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            ResponseBoolGoalDTO result = (ResponseBoolGoalDTO) goalService.getGoal(uid, mid);

            Assertions.assertThat(result).isNotNull();

            verify(goalPersistenceController).read(eq(mid));
        }

    }

    @Nested
    @DisplayName("finalizeGoal")
    class finalizeGoal {
        @Test
        public void finalizeGoal_whenNotFinalized_delegatesToPersistenceAndReturnsDTO() {
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            User user = new User(uid, "apellidos", "nombre");
            when(goalPersistenceController.updateWithUserStats(
                    any(Document.class), any(Document.class), any(Document.class), eq(uid), eq(mid)
            )).thenReturn(user);

            ResponseUserDTO result = goalService.finalizeGoal(uid, mid);

            Assertions.assertThat(result).isNotNull();
            verify(goalPersistenceController).read(eq(mid));
            verify(goalPersistenceController).updateWithUserStats(
                    any(Document.class), any(Document.class), any(Document.class), eq(uid), eq(mid)
            );
        }

        @Test
        public void finalizeGoal_whenAlreadyFinalized_throwsConflictException() {
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), true, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() -> goalService.finalizeGoal(uid, mid))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("La meta ya esta finalizada");

            verify(goalPersistenceController).read(eq(mid));
        }
    }

    @Nested
    @DisplayName("updateBoolGoal")
    class UpdateBoolGoal {
        @Test
        public void updateBoolGoal_whenValid_delegatesToPersistenceAndReturnsDTO() {
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setNombre("nuevoNombre");
            dto.setDescripcion("nuevaDesc");
            dto.setDuracionValor(3);
            dto.setDuracionUnidad(Enums.Duracion.Semanas);

            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "old", "oldDesc",
                    start, false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());

            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            User updatedUser = new User(uid, "apellidos", "nombre");
            when(goalPersistenceController.updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)))
                    .thenReturn(updatedUser);

            ResponseUserDTO result = goalService.updateBoolGoal(dto, uid, mid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getNombre()).isEqualTo("nombre");
            Assertions.assertThat(result.getApellidos()).isEqualTo("apellidos");
            verify(goalPersistenceController).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }

        @Test
        public void updateBoolGoal_whenFinalized_throwsConflictException() {
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setNombre("nuevoNombre");

            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), true, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() ->
                            goalService.updateBoolGoal(dto, uid, mid)
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("La meta ya esta finalizada, no puedes modificarla");

            verify(goalPersistenceController, never()).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }

        @Test
        public void updateBoolGoal_whenTipoNotBool_throwsBadRequestException() {
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setNombre("nuevoNombre");

            NumGoal goal = new NumGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    100.0, "km");
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() ->
                            goalService.updateBoolGoal(dto, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("Tipo incorrecto para la meta");

            verify(goalPersistenceController, never()).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }
    }

    @Nested
    @DisplayName("updateNumGoal")
    class UpdateNumGoal {
        @Test
        public void updateNumGoal_whenValid_delegatesToPersistenceAndReturnsDTO() {
            PatchNumGoalDTO dto = new PatchNumGoalDTO();
            dto.setValorObjetivo(50.0);
            dto.setUnidad("km");

            NumGoal goal = new NumGoal(uid, new ObjectId(), "oldName", "oldDesc",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    100.0, "m");
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            User updated = new User(uid, "newApellidos", "newNombre");
            when(goalPersistenceController.updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)))
                    .thenReturn(updated);

            ResponseUserDTO result = goalService.updateNumGoal(dto, uid, mid);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getNombre()).isEqualTo("newNombre");
            Assertions.assertThat(result.getApellidos()).isEqualTo("newApellidos");
            verify(goalPersistenceController).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }

        @Test
        public void updateNumGoal_whenFinalized_throwsConflictException() {
            PatchNumGoalDTO dto = new PatchNumGoalDTO();
            dto.setValorObjetivo(50.0);

            NumGoal goal = new NumGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), true, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    100.0, "m");
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() ->
                            goalService.updateNumGoal(dto, uid, mid)
                    ).isInstanceOf(ConflictException.class)
                    .hasMessage("La meta ya esta finalizada, no puedes modificarla");

            verify(goalPersistenceController, never()).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }

        @Test
        public void updateNumGoal_whenTipoNotNum_throwsBadRequestException() {
            PatchNumGoalDTO dto = new PatchNumGoalDTO();
            dto.setValorObjetivo(50.0);

            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() ->
                            goalService.updateNumGoal(dto, uid, mid)
                    ).isInstanceOf(BadRequestException.class)
                    .hasMessage("Tipo incorrecto para la meta");

            verify(goalPersistenceController, never()).updateWithGoalStats(
                    any(Document.class),
                    any(Document.class),
                    any(Document.class),
                    eq(uid),
                    eq(mid)
            );
        }
    }

    @Nested
    @DisplayName("deleteGoal")
    class DeleteGoal {

        @Test
        public void deleteGoal_whenGoalFinalizado_usesStatsDeltaMinus1Minus1() {
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), true, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);
            when(goalPersistenceController.delete(any(Document.class), eq(uid), eq(mid)))
                    .thenReturn(new UserStats(0, 0));

            ResponseUserStatsDTO result = goalService.deleteGoal(uid, mid);

            Assertions.assertThat(result).isNotNull();

            verify(goalPersistenceController).read(eq(mid));
            verify(statsService).getUserStatsUpdateDoc(-1, -1);
            verify(goalPersistenceController).delete(any(Document.class), eq(uid), eq(mid));
        }

        @Test
        public void deleteGoal_whenGoalNotFinalizado_usesStatsDeltaMinus1Zero() {
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);
            when(goalPersistenceController.delete(any(Document.class), eq(uid), eq(mid)))
                    .thenReturn(new UserStats(0, 0));

            ResponseUserStatsDTO result = goalService.deleteGoal(uid, mid);

            Assertions.assertThat(result).isNotNull();

            verify(goalPersistenceController).read(eq(mid));
            verify(statsService).getUserStatsUpdateDoc(-1, 0);
            verify(goalPersistenceController).delete(any(Document.class), eq(uid), eq(mid));
        }
    }

    @Nested
    @DisplayName("validateAndGetGoal")
    class ValidateAndGetGoal {
        @Test
        public void validateAndGetGoal_whenUidMatches_returnsGoal() {
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Goal result = goalService.validateAndGetGoal(uid, mid);

            Assertions.assertThat(result).isSameAs(goal);
            verify(goalPersistenceController).read(eq(mid));
        }

        @Test
        public void validateAndGetGoal_whenUidMismatch_throwsForbiddenResourceException() {
            BoolGoal goal = new BoolGoal("other-uid", new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>());
            when(goalPersistenceController.read(eq(mid))).thenReturn(goal);

            Assertions.assertThatThrownBy(() ->
                            goalService.validateAndGetGoal(uid, mid)
                    ).isInstanceOf(ForbiddenResourceException.class)
                    .hasMessage("No autorizado para acceder a esta meta");

            verify(goalPersistenceController).read(eq(mid));
        }

    }
}