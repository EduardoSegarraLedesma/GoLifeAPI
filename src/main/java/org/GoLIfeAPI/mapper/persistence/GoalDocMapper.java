package org.GoLIfeAPI.mapper.persistence;

import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.*;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalDocMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final RecordDocMapper recordDocMapper;

    public GoalDocMapper(RecordDocMapper recordDocMapper) {
        this.recordDocMapper = recordDocMapper;
    }

    // Map POJOs to DOCs

    public Document mapBoolGoalToDoc(BoolGoal goal) {
        Document doc = new Document();
        mapGoalToDoc(goal, doc);
        doc.append("estadisticas", mapGoalStatsToDoc(goal.getEstadisticas()));
        if (goal.getRegistros() != null) {
            List<Document> registrosDocs = goal.getRegistros()
                    .stream()
                    .map(record -> recordDocMapper.mapBoolRecordToDoc(record))
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }
        return doc;
    }

    public Document mapNumGoalToDoc(NumGoal goal) {
        Document doc = new Document();
        mapGoalToDoc(goal, doc);
        doc.append("valorObjetivo", goal.getValorObjetivo());
        doc.append("unidad", goal.getUnidad());
        doc.append("estadisticas", mapGoalStatsToDoc(goal.getEstadisticas()));
        if (goal.getRegistros() != null) {
            List<Document> registrosDocs = goal.getRegistros()
                    .stream()
                    .map(record -> recordDocMapper.mapNumRecordToDoc(record))
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }
        return doc;
    }

    public Document mapGoalToPartialGoalDoc(Goal goal) {
        return new Document()
                .append("_id", goal.get_id())
                .append("nombre", goal.getNombre())
                .append("tipo", goal.getTipo())
                .append("fecha", goal.getFecha().format(formatter))
                .append("finalizado", goal.getFinalizado())
                .append("duracionValor", goal.getDuracionValor())
                .append("duracionUnidad", goal.getDuracionUnidad());
    }

    public Document mapPartialGoalToDoc(PartialGoal goal) {
        return new Document()
                .append("_id", goal.get_id())
                .append("nombre", goal.getNombre())
                .append("tipo", goal.getTipo())
                .append("fecha", goal.getFecha().format(formatter))
                .append("finalizado", goal.getFinalizado())
                .append("duracionValor", goal.getDuracionValor())
                .append("duracionUnidad", goal.getDuracionUnidad());
    }

    private void mapGoalToDoc(Goal goal, Document doc) {
        doc.append("uid", goal.getUid());
        doc.append("nombre", goal.getNombre());
        doc.append("tipo", goal.getTipo());
        doc.append("descripcion", goal.getDescripcion());
        doc.append("fecha", goal.getFecha().format(formatter));
        doc.append("finalizado", goal.getFinalizado());
        doc.append("duracionValor", goal.getDuracionValor());
        doc.append("duracionUnidad", goal.getDuracionUnidad());
    }

    private Document mapGoalStatsToDoc(GoalStats goalStats) {
        LocalDate finalDate = goalStats.getFechaFin();
        return new Document()
                .append("valorAlcanzado", goalStats.getValorAlcanzado())
                .append("fechaFin", finalDate != null ? finalDate.format(formatter) : "");
    }

    // Map DOCs to POJOs

    public Goal mapDocToGoal(Document goalDoc) {
        String type = goalDoc.getString("tipo");
        if (type.equals("Bool"))
            return mapDocToBoolGoal(goalDoc);
        else if (type.equals("Num"))
            return mapDocToNumGoal(goalDoc);
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public BoolGoal mapDocToBoolGoal(Document boolGoalDoc) {
        if(boolGoalDoc == null || boolGoalDoc.isEmpty()) return null;
        List<Document> boolRecords = boolGoalDoc.getList("registros", Document.class);
        List<BoolRecord> registros = Collections.emptyList();
        if (boolRecords != null) {
            registros = boolRecords.
                    stream().
                    map(record -> recordDocMapper.mapDocToBoolRecord(record))
                    .collect(Collectors.toList());
        }
        return new BoolGoal(
                boolGoalDoc.getString("uid"),
                boolGoalDoc.getObjectId("_id"),
                boolGoalDoc.getString("nombre"),
                boolGoalDoc.getString("descripcion"),
                LocalDate.parse(boolGoalDoc.getString("fecha")),
                boolGoalDoc.getBoolean("finalizado"),
                boolGoalDoc.getInteger("duracionValor"),
                Enums.Duracion.valueOf(boolGoalDoc.getString("duracionUnidad")),
                mapDocToGoalStats(boolGoalDoc.get("estadisticas", Document.class)),
                registros
        );
    }

    public NumGoal mapDocToNumGoal(Document numGoalDoc) {
        if(numGoalDoc == null || numGoalDoc.isEmpty()) return null;
        List<Document> numrecords = numGoalDoc.getList("registros", Document.class);
        List<NumRecord> registros = Collections.emptyList();
        if (numrecords != null) {
            registros = numrecords.
                    stream().
                    map(record -> recordDocMapper.mapDocToNumRecord(record))
                    .collect(Collectors.toList());
        }
        return new NumGoal(
                numGoalDoc.getString("uid"),
                numGoalDoc.getObjectId("_id"),
                numGoalDoc.getString("nombre"),
                numGoalDoc.getString("descripcion"),
                LocalDate.parse(numGoalDoc.getString("fecha")),
                numGoalDoc.getBoolean("finalizado"),
                numGoalDoc.getInteger("duracionValor"),
                Enums.Duracion.valueOf(numGoalDoc.getString("duracionUnidad")),
                mapDocToGoalStats(numGoalDoc.get("estadisticas", Document.class)),
                registros,
                numGoalDoc.getDouble("valorObjetivo"),
                numGoalDoc.getString("unidad")
                );
    }


    public PartialGoal mapDocToPartialGoal(Document partialGoalDoc) {
        return new PartialGoal(
                partialGoalDoc.getObjectId("_id"),
                partialGoalDoc.getString("nombre"),
                Enums.Tipo.valueOf(partialGoalDoc.getString("tipo")),
                LocalDate.parse(partialGoalDoc.getString("fecha")),
                partialGoalDoc.getBoolean("finalizado"),
                partialGoalDoc.getInteger("duracionValor"),
                Enums.Duracion.valueOf(partialGoalDoc.getString("duracionUnidad"))
        );
    }

    private GoalStats mapDocToGoalStats(Document goalStatsDoc) {
        return new GoalStats(
                goalStatsDoc.getBoolean("valorAlcanzado"),
                !goalStatsDoc.getString("fechaFin").isEmpty()? LocalDate.parse(goalStatsDoc.getString("fechaFin")) : null );
    }
}
