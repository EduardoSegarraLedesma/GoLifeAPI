package org.GoLIfeAPI.bussiness;

import org.GoLIfeAPI.dto.goal.PatchGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StatsService {

    // User Stats
    public Document getUserStatsUpdateDoc(int deltaTotalGoals, int deltaTotalFinalizedGoals) {
        return new Document()
                .append("totalMetas", deltaTotalGoals)
                .append("totalMetasFinalizadas", deltaTotalFinalizedGoals);
    }

    //Goal Stats
    public LocalDate calculateFinalGoalDate(LocalDate startDate, int duration, Enums.Duracion durationUnit) {
        return switch (durationUnit) {
            case Dias -> startDate.plusDays(duration);
            case Semanas -> startDate.plusWeeks(duration);
            case Meses -> startDate.plusMonths(duration);
            case Años -> startDate.plusYears(duration);
            case Indefinido -> null;
        };
    }

    public Document getGoalStatsFinalDateUpdateDoc(PatchGoalDTO goalDto, Goal goal) {
        Integer newDurationValue = goalDto.getDuracionValor();
        Enums.Duracion newDurationUnit = goalDto.getDuracionUnidad();
        Document goalStatsUpdates = new Document();
        LocalDate startDate = LocalDate.parse(goal.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE));
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate newFinalDate;
        if ((newDurationValue != null && newDurationValue > 0) && newDurationUnit == null) {
            newFinalDate = calculateFinalGoalDate(startDate, newDurationValue, goal.getDuracionUnidad());
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        } else if ((newDurationValue == null || newDurationValue < 0) && newDurationUnit != null) {
            newFinalDate = calculateFinalGoalDate(startDate,
                    goal.getDuracionValor() > 0 ? goal.getDuracionValor() : 1,
                    newDurationUnit);
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        } else if ((newDurationValue != null && newDurationValue > 0) && newDurationUnit != null) {
            newFinalDate = calculateFinalGoalDate(startDate, newDurationValue, newDurationUnit);
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        }
        return goalStatsUpdates;
    }

    public Document getGoalStatsReachedBoolValueUpdateDoc(CreateBoolRecordDTO recordDto, BoolGoal boolGoal) {
        Document goalStatsUpdates = new Document();
        Boolean ReachedValue = boolGoal.getEstadisticas().getValorAlcanzado();
        if (!ReachedValue && recordDto.isValorBool())
            goalStatsUpdates.append("valorAlcanzado", true);
        return goalStatsUpdates;
    }

    public Document getGoalStatsReachedNumValueUpdateDoc(CreateNumRecordDTO recordDto, NumGoal numGoal) {
        Document goalStatsUpdates = new Document();
        Boolean ReachedValue = numGoal.getEstadisticas().getValorAlcanzado();
        double goalValue = numGoal.getValorObjetivo();
        if (!ReachedValue && compareDoublesWithTwoDecimals(recordDto.getValorNum(), goalValue))
            goalStatsUpdates.append("valorAlcanzado", true);
        return goalStatsUpdates;
    }

    private Boolean compareDoublesWithTwoDecimals(Double value1, Double value2) {
        long v1 = Math.round(value1 * 100);
        long v2 = Math.round(value2 * 100);
        return v1 >= v2;
    }
}