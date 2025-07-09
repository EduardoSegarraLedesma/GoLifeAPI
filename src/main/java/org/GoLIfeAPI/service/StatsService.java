package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.PatchGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseGoalStatsDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.model.Enums;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StatsService {

    // User Stats
    public Document getUpdatedUserStatsDoc(int deltaTotalGoals, int deltaTotalFinalizedGoals) {
        return new Document()
                .append("totalMetas", deltaTotalGoals)
                .append("totalMetasFinalizadas", deltaTotalFinalizedGoals);
    }

    public ResponseUserStatsDTO mapToResponseUserStatsDTO(Document doc) {
        return new ResponseUserStatsDTO(
                doc.getInteger("totalMetas"),
                doc.getInteger("totalMetasFinalizadas"));
    }

    public ResponseUserStatsDTO mapEmbeddedToResponseUserStatsDTO(Document doc) {
        Document statsDoc = doc.get("estadisticas", Document.class);
        return new ResponseUserStatsDTO(
                statsDoc.getInteger("totalMetas"),
                statsDoc.getInteger("totalMetasFinalizadas"));
    }

    //Goal Stats
    public LocalDate calculateFinalGoalDate(LocalDate startDate, int duration, Enums.Duracion durationUnit) {
        return switch (durationUnit) {
            case Dias -> startDate.plusDays(duration);
            case Semanas -> startDate.plusWeeks(duration);
            case Meses -> startDate.plusMonths(duration);
            case Años -> startDate.plusYears(duration);
            default -> null;
        };
    }

    public Document getGoalStatsFinalDateUpdate(PatchGoalDTO goalDto, Document goalDoc) {
        Integer newDuration = goalDto.getDuracionValor();
        Enums.Duracion newDurationUnit = goalDto.getDuracionUnidad();
        Document goalStatsUpdates = new Document();
        LocalDate startDate = LocalDate.parse(goalDoc.getString("fecha"), DateTimeFormatter.ISO_LOCAL_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate newFinalDate;
        if ((newDuration != null && newDuration > 0) && newDurationUnit == null) {
            newFinalDate = calculateFinalGoalDate(
                    startDate,
                    newDuration,
                    Enums.Duracion.valueOf(goalDoc.getString("duracionUnidad")));
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        } else if ((newDuration == null || newDuration < 0) && newDurationUnit != null) {
            newFinalDate = calculateFinalGoalDate(
                    startDate,
                    goalDoc.getInteger("duracionValor"),
                    newDurationUnit);
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        } else if ((newDuration != null && newDuration > 0) && newDurationUnit != null) {
            newFinalDate = calculateFinalGoalDate(
                    startDate,
                    goalDoc.getInteger("duracionValor"),
                    newDurationUnit);
            goalStatsUpdates.append("fechaFin", newFinalDate != null ? newFinalDate.format(formatter) : "");
        }
        return goalStatsUpdates;
    }

    public Document getGoalStatsReachedBoolValueUpdate(CreateBoolRecordDTO recordDto, Document goalDoc) {
        Document goalStatsUpdates = new Document();
        Boolean ReachedValue = goalDoc.get("estadisticas", Document.class).getBoolean("valorAlcanzado");
        if (!ReachedValue && recordDto.isValorBool())
            goalStatsUpdates.append("valorAlcanzado", true);
        return goalStatsUpdates;
    }

    public Document getGoalStatsReachedNumValueUpdate(CreateNumRecordDTO recordDto, Document goalDoc) {
        Document goalStatsUpdates = new Document();
        Boolean ReachedValue = goalDoc.get("estadisticas", Document.class).getBoolean("valorAlcanzado");
        double goalValue = goalDoc.getDouble("valorObjetivo");
        if (!ReachedValue && compareDoublesWithTwoDecimals(recordDto.getValorNum(), goalValue))
            goalStatsUpdates.append("valorAlcanzado", true);
        return goalStatsUpdates;
    }

    public ResponseGoalStatsDTO mapEmbeddedToResponseGoalStatsDTO(Document newDoc, Document oldDoc) {
        Document newStatsDoc = newDoc.get("estadisticas", Document.class);
        Document oldStatsDoc = oldDoc.get("estadisticas", Document.class);
        return new ResponseGoalStatsDTO(
                newStatsDoc.getBoolean("valorAlcanzado"),
                (newStatsDoc.getBoolean("valorAlcanzado") && !oldStatsDoc.getBoolean("valorAlcanzado")),
                newStatsDoc.getString("fechaFin"));
    }

    private Boolean compareDoublesWithTwoDecimals(Double value1, Double value2) {
        long v1 = Math.round(value1 * 100);
        long v2 = Math.round(value2 * 100);
        return v1 >= v2;
    }
}