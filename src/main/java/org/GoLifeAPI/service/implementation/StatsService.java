package org.GoLifeAPI.service.implementation;

import org.GoLifeAPI.dto.goal.PatchGoalDTO;
import org.GoLifeAPI.model.Enums;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.Goal;
import org.GoLifeAPI.model.goal.NumGoal;
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
        } else if (newDurationValue != null && newDurationValue > 0) {
            newFinalDate = calculateFinalGoalDate(startDate, newDurationValue, newDurationUnit);
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        }
        return goalStatsUpdates;
    }

    public Document getGoalStatsHasFirstRecordUpdateDoc(Goal goal) {
        Document goalStatsUpdates = new Document();
        if (!goal.getEstadisticas().getTienePrimerRegistro())
            if (goal instanceof BoolGoal bool) {
                if (bool.getRegistros() != null && bool.getRegistros().isEmpty())
                    goalStatsUpdates.append("tienePrimerRegistro", true);
            } else if (goal instanceof NumGoal num) {
                if (num.getRegistros() != null && num.getRegistros().isEmpty())
                    goalStatsUpdates.append("tienePrimerRegistro", true);
            }
        return goalStatsUpdates;
    }
}