package org.GoLifeAPI.mixed.service;

import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchGoalDTO;
import org.GoLifeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLifeAPI.dto.record.CreateNumRecordDTO;
import org.GoLifeAPI.model.Enums;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.GoalStats;
import org.GoLifeAPI.model.goal.NumGoal;
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
    @DisplayName("getGoalStatsReachedBoolValueUpdateDoc")
    class GetGoalStatsReachedBoolValueUpdateDoc {
        @Test
        public void getGoalStatsReachedBoolValueUpdateDoc_whenNotReachedAndRecordTrue_returnsDocWithTrue() {
            CreateBoolRecordDTO recordDto = new CreateBoolRecordDTO(true, LocalDate.now());
            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.now(), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.now()), new ArrayList<>());

            Document doc = statsService.getGoalStatsReachedBoolValueUpdateDoc(recordDto, boolGoal);

            Assertions.assertThat(doc.containsKey("valorAlcanzado")).isTrue();
            Assertions.assertThat(doc.getBoolean("valorAlcanzado")).isTrue();
        }

        @Test
        public void getGoalStatsReachedBoolValueUpdateDoc_whenAlreadyReachedAndRecordTrue_returnsEmptyDoc() {
            CreateBoolRecordDTO recordDto = new CreateBoolRecordDTO(true, LocalDate.now());
            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.now(), false, 1, Enums.Duracion.Dias,
                    new GoalStats(true, LocalDate.now()), new ArrayList<>());

            Document doc = statsService.getGoalStatsReachedBoolValueUpdateDoc(recordDto, boolGoal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsReachedBoolValueUpdateDoc_whenNotReachedAndRecordFalse_returnsEmptyDoc() {
            CreateBoolRecordDTO recordDto = new CreateBoolRecordDTO(false, LocalDate.now());
            BoolGoal boolGoal = new BoolGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.now(), false, 1, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.now()), new ArrayList<>());

            Document doc = statsService.getGoalStatsReachedBoolValueUpdateDoc(recordDto, boolGoal);

            Assertions.assertThat(doc.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getGoalStatsReachedNumValueUpdateDoc")
    class GetGoalStatsReachedNumValueUpdateDoc {
        @Test
        public void getGoalStatsReachedNumValueUpdateDoc_whenNotReachedAndValueEqualsGoal_returnsDocWithTrue() {
            CreateNumRecordDTO recordDto = new CreateNumRecordDTO(123.45, LocalDate.of(2025, 7, 1));
            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    123.45, "unit");
            Document doc = statsService.getGoalStatsReachedNumValueUpdateDoc(recordDto, numGoal);
            Assertions.assertThat(doc.containsKey("valorAlcanzado")).isTrue();
            Assertions.assertThat(doc.getBoolean("valorAlcanzado")).isTrue();
        }

        @Test
        public void getGoalStatsReachedNumValueUpdateDoc_whenAlreadyReached_returnsEmptyDoc() {
            CreateNumRecordDTO recordDto = new CreateNumRecordDTO(123.45, LocalDate.of(2025, 7, 1));
            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(true, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    123.45, "unit");
            Document doc = statsService.getGoalStatsReachedNumValueUpdateDoc(recordDto, numGoal);
            Assertions.assertThat(doc.isEmpty()).isTrue();
        }

        @Test
        public void getGoalStatsReachedNumValueUpdateDoc_whenValueDiffers_returnsEmptyDoc() {
            CreateNumRecordDTO recordDto = new CreateNumRecordDTO(100.00, LocalDate.of(2025, 7, 1));
            NumGoal numGoal = new NumGoal(uid, new ObjectId(), "n", "d",
                    LocalDate.of(2025, 7, 1), false, 5, Enums.Duracion.Dias,
                    new GoalStats(false, LocalDate.of(2025, 7, 1)), new ArrayList<>(),
                    123.45, "unit");
            Document doc = statsService.getGoalStatsReachedNumValueUpdateDoc(recordDto, numGoal);
            Assertions.assertThat(doc.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("compareDoublesWithTwoDecimals")
    class CompareDoublesWithTwoDecimals {
        @Test
        public void compareDoublesWithTwoDecimals_whenEqualValues_returnsTrue() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    1.23, 1.23
            );
            Assertions.assertThat(result).isTrue();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenValue1Greater_returnsTrue() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    1.235, 1.23
            );
            Assertions.assertThat(result).isTrue();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenValue1Less_returnsFalse() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    1.234, 1.24
            );
            Assertions.assertThat(result).isFalse();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenRoundedUpEdge_returnsTrue() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    1.005, 1.00
            );
            Assertions.assertThat(result).isTrue();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenValuesEqual_returnsTrue() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    2.345, 2.345
            );
            Assertions.assertThat(result).isTrue();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenValue1JustBelowThreshold_returnsFalse() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    1.2349, 1.235
            );
            Assertions.assertThat(result).isFalse();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenZeroAndVerySmall_returnsTrue() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    0.004, 0.0
            );
            Assertions.assertThat(result).isTrue();
        }

        @Test
        public void compareDoublesWithTwoDecimals_whenNegativeValue1LessThanValue2_returnsFalse() {
            Boolean result = ReflectionTestUtils.invokeMethod(
                    statsService,
                    "compareDoublesWithTwoDecimals",
                    -1.236, -1.23
            );
            Assertions.assertThat(result).isFalse();
        }
    }
}