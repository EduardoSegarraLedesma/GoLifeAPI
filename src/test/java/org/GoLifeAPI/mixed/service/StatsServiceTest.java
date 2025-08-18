package org.GoLifeAPI.mixed.service;

import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchGoalDTO;
import org.GoLifeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLifeAPI.dto.record.CreateNumRecordDTO;
import org.GoLifeAPI.model.Enums;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.GoalStats;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.model.record.BoolRecord;
import org.GoLifeAPI.model.record.NumRecord;
import org.GoLifeAPI.service.implementation.StatsService;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @InjectMocks
    private StatsService statsService;

    private String uid;

    @BeforeEach
    void setUp() {
        uid = "test-uid";
    }

    @Nested
    @DisplayName("getUserStatsUpdateDoc")
    class GetUserStatsUpdateDoc {
        @Test
        public void getUserStatsUpdateDoc_whenPositiveDeltas_returnsDocumentWithFields() {
            Document doc = statsService.getUserStatsUpdateDoc(5, 2);
            Assertions.assertThat(doc.getInteger("totalMetas")).isEqualTo(5);
            Assertions.assertThat(doc.getInteger("totalMetasFinalizadas")).isEqualTo(2);
        }

        @Test
        public void getUserStatsUpdateDoc_whenZeroDeltas_returnsDocumentWithZeros() {
            Document doc = statsService.getUserStatsUpdateDoc(0, 0);
            Assertions.assertThat(doc.getInteger("totalMetas")).isEqualTo(0);
            Assertions.assertThat(doc.getInteger("totalMetasFinalizadas")).isEqualTo(0);
        }

        @Test
        public void getUserStatsUpdateDoc_whenNegativeDeltas_returnsDocumentWithNegativeFields() {
            Document doc = statsService.getUserStatsUpdateDoc(-3, -1);
            Assertions.assertThat(doc.getInteger("totalMetas")).isEqualTo(-3);
            Assertions.assertThat(doc.getInteger("totalMetasFinalizadas")).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("calculateFinalGoalDate")
    class CalculateFinalGoalDate {
        @Test
        public void calculateFinalGoalDate_whenUnitDias_addsDays() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            LocalDate result = statsService.calculateFinalGoalDate(start, 5, Enums.Duracion.Dias);
            Assertions.assertThat(result).isEqualTo(LocalDate.of(2025, 7, 6));
        }

        @Test
        public void calculateFinalGoalDate_whenUnitSemanas_addsWeeks() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            LocalDate result = statsService.calculateFinalGoalDate(start, 2, Enums.Duracion.Semanas);
            Assertions.assertThat(result).isEqualTo(LocalDate.of(2025, 7, 15));
        }

        @Test
        public void calculateFinalGoalDate_whenUnitMeses_addsMonths() {
            LocalDate start = LocalDate.of(2025, 1, 31);
            LocalDate result = statsService.calculateFinalGoalDate(start, 1, Enums.Duracion.Meses);
            Assertions.assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
        }

        @Test
        public void calculateFinalGoalDate_whenUnitAños_addsYears() {
            LocalDate start = LocalDate.of(2024, 2, 29);
            LocalDate result = statsService.calculateFinalGoalDate(start, 1, Enums.Duracion.Años);
            Assertions.assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
        }

        @Test
        public void calculateFinalGoalDate_whenUnitIndefinido_returnsNull() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            LocalDate result = statsService.calculateFinalGoalDate(start, 10, Enums.Duracion.Indefinido);
            Assertions.assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getGoalStatsFinalDateUpdateDoc")
    class GetGoalStatsFinalDateUpdateDoc {
        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNewValueNoNewUnit_usesOriginalUnit() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(5);
            dto.setDuracionUnidad(null);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 3, Enums.Duracion.Semanas,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            String expected = start.plusWeeks(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo(expected);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNewValueNoNewUnitIndefinido_usesOriginalUnitReturnsBlank() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(5);
            dto.setDuracionUnidad(null);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 3, Enums.Duracion.Indefinido,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo("");
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNoNewValueNewUnit_usesOriginalValueOrOne() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(null);
            dto.setDuracionUnidad(Enums.Duracion.Meses);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 4, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            String expected = start.plusMonths(4).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo(expected);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNoNewValueNewUnitIndefinido_usesOriginalValueOrOneReturnsBlank() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(null);
            dto.setDuracionUnidad(Enums.Duracion.Indefinido);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 4, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo("");
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNewValueAndUnitNull_returnsEmptyFechaFin() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(null);
            dto.setDuracionUnidad(null);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());

            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo(null);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNewValueNegativeAndUnitProvided_usesOriginal() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(-3);
            dto.setDuracionUnidad(Enums.Duracion.Semanas);

            BoolGoal goal1 = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc1 = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal1);
            String expected1 = start.plusWeeks(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc1.getString("fechaFin")).isEqualTo(expected1);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenNewValueNegativeAndUnitProvided_oneWithNewUnit() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(-3);
            dto.setDuracionUnidad(Enums.Duracion.Semanas);

            BoolGoal goal2 = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 0, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc2 = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal2);
            String expected2 = start.plusWeeks(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc2.getString("fechaFin")).isEqualTo(expected2);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenValuePositiveAndUnitProvided_usesThatUnit() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(7);
            dto.setDuracionUnidad(Enums.Duracion.Meses);

            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false,
                    10, Enums.Duracion.Semanas,
                    new GoalStats(false, start), new ArrayList<>());

            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            String expected = start.plusMonths(7).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo(expected);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenBothNewValueAndNewUnit_usesBoth() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(2);
            dto.setDuracionUnidad(Enums.Duracion.Años);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 4, Enums.Duracion.Meses,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            String expected = start.plusYears(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo(expected);
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenBothNewValueAndNewUnitIndefinido_usesBothReturnsBlank() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(2);
            dto.setDuracionUnidad(Enums.Duracion.Indefinido);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 4, Enums.Duracion.Meses,
                    new GoalStats(false, start), new ArrayList<>());
            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            Assertions.assertThat(doc.getString("fechaFin")).isEqualTo("");
        }

        @Test
        public void getGoalStatsFinalDateUpdateDoc_whenZeroValueAndNoUnit_thenDocIsEmpty() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            PatchBoolGoalDTO dto = new PatchBoolGoalDTO();
            dto.setDuracionValor(0);
            dto.setDuracionUnidad(null);
            BoolGoal goal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    start, false, 5, Enums.Duracion.Semanas,
                    new GoalStats(false, start), new ArrayList<>());

            Document doc = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

    }

    @Nested
    @DisplayName("getGoalStatsHasFirstRecordUpdateDoc")
    class GetGoalStatsHasFirstRecordUpdateDoc {

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenBoolNoFirstAndEmptyList_setsFlagTrue() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal goal = new BoolGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 3, Enums.Duracion.Semanas,
                    new GoalStats(false, start), new ArrayList<>()
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.containsKey("tienePrimerRegistro")).isTrue();
            Assertions.assertThat(doc.getBoolean("tienePrimerRegistro")).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenNumNoFirstAndEmptyList_setsFlagTrue() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal goal = new NumGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 2, Enums.Duracion.Meses,
                    new GoalStats(false, start), new ArrayList<>(),
                    10.0, "u"
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.containsKey("tienePrimerRegistro")).isTrue();
            Assertions.assertThat(doc.getBoolean("tienePrimerRegistro")).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenAlreadyHasFirstRecord_returnsEmptyDoc() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal goal = new BoolGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, start), new ArrayList<>()
            );
            // Forzamos tienePrimerRegistro = true
            ReflectionTestUtils.setField(goal.getEstadisticas(), "tienePrimerRegistro", true);

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenBoolListIsNull_returnsEmptyDoc() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            BoolGoal goal = new BoolGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 3, Enums.Duracion.Semanas,
                    new GoalStats(false, start), null
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenBoolListNotEmpty_returnsEmptyDoc() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            List<BoolRecord> registros = new ArrayList<>();
            registros.add(new BoolRecord(true, start));

            BoolGoal goal = new BoolGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 3, Enums.Duracion.Semanas,
                    new GoalStats(false, start), registros
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenNumListIsNull_returnsEmptyDoc() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            NumGoal goal = new NumGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 2, Enums.Duracion.Meses,
                    new GoalStats(false, start), null,
                    10.0, "u"
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsHasFirstRecordUpdateDoc_whenNumListNotEmpty_returnsEmptyDoc() {
            LocalDate start = LocalDate.of(2025, 7, 1);
            List<NumRecord> registros = new ArrayList<>();
            registros.add(new NumRecord(3.14, start));

            NumGoal goal = new NumGoal(
                    uid, new ObjectId(), "n", "d",
                    start, false, 2, Enums.Duracion.Meses,
                    new GoalStats(false, start), registros,
                    10.0, "u"
            );

            Document doc = statsService.getGoalStatsHasFirstRecordUpdateDoc(goal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }
    }

}