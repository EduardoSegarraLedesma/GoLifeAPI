package org.GoLIfeAPI.mapper;

import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.GoalStats;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final RecordMapper recordMapper;


    public GoalMapper(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    // Map Input DTOs to POJOs

    public BoolGoal mapCreateBoolGoalDtoToBoolGoal(CreateBoolGoalDTO newGoalDto, String uid, LocalDate fechaFin) {
        return new BoolGoal(
                uid, newGoalDto.getNombre(), newGoalDto.getDescripcion(), newGoalDto.getFecha(), false,
                newGoalDto.getDuracionValor(), newGoalDto.getDuracionUnidad(), false, fechaFin);
    }

    public NumGoal mapCreateNumGoalDtoToNumGoal(CreateNumGoalDTO newGoalDto, String uid, LocalDate fechaFin) {
        return new NumGoal(
                uid, newGoalDto.getNombre(), newGoalDto.getDescripcion(), newGoalDto.getFecha(), false,
                newGoalDto.getDuracionValor(), newGoalDto.getDuracionUnidad(), false, fechaFin,
                newGoalDto.getValorObjetivo(), newGoalDto.getUnidad());
    }

    // Map Input DTOs to Docs

    public Document mapPatchBoolGoalDtoToDoc(PatchBoolGoalDTO dto) {
        Document doc = new Document();
        mapPatchGoalDtoToDoc(dto, doc);
        return doc;
    }

    public Document mapPatchNumGoalDtoToDoc(PatchNumGoalDTO dto) {
        Document doc = new Document();
        Double goalValue = dto.getValorObjetivo();
        String unit = dto.getUnidad();
        mapPatchGoalDtoToDoc(dto, doc);
        if (goalValue != null) doc.append("valorObjetivo", goalValue);
        if (unit != null && !unit.isBlank()) doc.append("unidad", unit);
        return doc;
    }

    public Document mapPatchGoalDtoToPartialDoc(PatchGoalDTO dto) {
        Document doc = new Document();
        String name = dto.getNombre();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        appendCorrectDuracionValor(doc, dto);
        if (durationUnit != null) doc.append("duracionUnidad", durationUnit);
        return doc;
    }

    private void mapPatchGoalDtoToDoc(PatchGoalDTO dto, Document doc) {
        String name = dto.getNombre();
        String desc = dto.getDescripcion();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        if (desc != null && !desc.isBlank()) doc.append("descripcion", desc);
        appendCorrectDuracionValor(doc, dto);
        if (durationUnit != null) doc.append("duracionUnidad", durationUnit);
    }

    private void appendCorrectDuracionValor(Document doc, PatchGoalDTO dto) {
        Integer durationValue = dto.getDuracionValor();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (durationUnit == null) {
            if (durationValue != null && durationValue > 0)
                doc.append("duracionValor", durationValue);
        } else if (durationUnit.toString().equalsIgnoreCase("Indefinido"))
            doc.append("duracionValor", -1);
        else {
            if (durationValue != null && durationValue > 0)
                doc.append("duracionValor", durationValue);
            else
                doc.append("duracionValor", 1);
        }
    }

    // Map POJOs to Docs

    public Document mapBoolGoalToBoolGoalDoc(BoolGoal goal) {
        Document doc = new Document();
        mapGoalToGoalDoc(goal, doc);
        doc.append("estadisticas", mapGoalStatsToGoalStatsDoc(goal.getEstadisticas()));
        if (goal.getRegistros() != null) {
            List<Document> registrosDocs = goal.getRegistros()
                    .stream()
                    .map(record -> recordMapper.mapBoolRecordToBoolRecordDoc(record))
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }
        return doc;
    }

    public Document mapNumGoalToNumGoalDoc(NumGoal goal) {
        Document doc = new Document();
        mapGoalToGoalDoc(goal, doc);
        doc.append("valorObjetivo", goal.getValorObjetivo());
        doc.append("unidad", goal.getUnidad());
        doc.append("estadisticas", mapGoalStatsToGoalStatsDoc(goal.getEstadisticas()));
        if (goal.getRegistros() != null) {
            List<Document> registrosDocs = goal.getRegistros()
                    .stream()
                    .map(record -> recordMapper.mapNumRecordToNumRecordDoc(record))
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }
        return doc;
    }

    public Document mapGoalToParcialDoc(Goal goal) {
        return new Document()
                .append("_id", goal.get_id())
                .append("nombre", goal.getNombre())
                .append("tipo", goal.getTipo())
                .append("fecha", goal.getFecha().format(formatter))
                .append("finalizado", goal.getFinalizado())
                .append("duracionValor", goal.getDuracionValor())
                .append("duracionUnidad", goal.getDuracionUnidad());
    }

    private void mapGoalToGoalDoc(Goal goal, Document doc) {
        doc.append("uid", goal.getUid());
        doc.append("nombre", goal.getNombre());
        doc.append("tipo", goal.getTipo());
        doc.append("descripcion", goal.getDescripcion());
        doc.append("fecha", goal.getFecha().format(formatter));
        doc.append("finalizado", goal.getFinalizado());
        doc.append("duracionValor", goal.getDuracionValor());
        doc.append("duracionUnidad", goal.getDuracionUnidad());
    }

    private Document mapGoalStatsToGoalStatsDoc(GoalStats goalStats) {
        LocalDate finalDate = goalStats.getFechaFin();
        return new Document()
                .append("valorAlcanzado", goalStats.getValorAlcanzado())
                .append("fechaFin", finalDate != null ? finalDate.format(formatter) : "");
    }

    // Map Docs to Output DTOs
    public Object mapGoalDocToResponseGoalDTO(Document newDoc, Document oldDoc) {
        String type = newDoc.getString("tipo");
        if (type.equals("Bool"))
            return mapGoalDocToResponseBoolGoalDTO(newDoc, oldDoc);
        else if (type.equals("Num"))
            return mapGoalDocToResponseNumGoalDTO(newDoc, oldDoc);
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public ResponseBoolGoalDTO mapGoalDocToResponseBoolGoalDTO(Document newDoc, Document oldDoc) {
        List<Document> boolRecords = newDoc.getList("registros", Document.class);
        List<ResponseBoolRecordDTO> registros = Collections.emptyList();
        if (boolRecords != null) {
            registros = boolRecords.
                    stream().
                    map(record -> recordMapper.mapBoolRecordDocToResponseBoolRecordDto(record))
                    .collect(Collectors.toList());
        }
        return new ResponseBoolGoalDTO(
                newDoc.getObjectId("_id").toString(),
                newDoc.getString("nombre"),
                Enums.Tipo.valueOf(newDoc.getString("tipo")),
                newDoc.getString("descripcion"),
                newDoc.getString("fecha"),
                newDoc.getBoolean("finalizado"),
                newDoc.getInteger("duracionValor"),
                Enums.Duracion.valueOf(newDoc.getString("duracionUnidad")),
                mapGoalDocToResponseGoalStatsDTO(newDoc, oldDoc),
                registros
        );
    }

    public ResponseNumGoalDTO mapGoalDocToResponseNumGoalDTO(Document newDoc, Document oldDoc) {
        List<Document> numRecords = newDoc.getList("registros", Document.class);
        List<ResponseNumRecordDTO> registros = Collections.emptyList();
        if (numRecords != null) {
            registros = numRecords
                    .stream().
                    map(record -> recordMapper.mapNumRecordDocToResponseNumRecordDto(record))
                    .collect(Collectors.toList());
        }
        return new ResponseNumGoalDTO(
                newDoc.getObjectId("_id").toString(),
                newDoc.getString("nombre"),
                Enums.Tipo.valueOf(newDoc.getString("tipo")),
                newDoc.getString("descripcion"),
                newDoc.getString("fecha"),
                newDoc.getBoolean("finalizado"),
                newDoc.getInteger("duracionValor"),
                Enums.Duracion.valueOf(newDoc.getString("duracionUnidad")),
                mapGoalDocToResponseGoalStatsDTO(newDoc, oldDoc),
                registros,
                newDoc.getDouble("valorObjetivo"),
                newDoc.getString("unidad")
        );
    }

    private ResponseGoalStatsDTO mapGoalDocToResponseGoalStatsDTO(Document newDoc, Document oldDoc) {
        Document newStatsDoc = newDoc.get("estadisticas", Document.class);
        Document oldStatsDoc = oldDoc.get("estadisticas", Document.class);
        return new ResponseGoalStatsDTO(
                newStatsDoc.getBoolean("valorAlcanzado"),
                (newStatsDoc.getBoolean("valorAlcanzado") && !oldStatsDoc.getBoolean("valorAlcanzado")),
                newStatsDoc.getString("fechaFin"));
    }

    public ResponsePartialGoalDTO mapGoalDocToPartialGoalDTO(Document partialGoal) {
        ObjectId id = partialGoal.getObjectId("_id");
        String nombre = partialGoal.getString("nombre");
        Enums.Tipo tipo = Enums.Tipo.valueOf(partialGoal.getString("tipo"));
        String fecha = partialGoal.getString("fecha");
        Boolean finalizado = partialGoal.getBoolean("finalizado");
        int duracionValor = partialGoal.getInteger("duracionValor");
        Enums.Duracion durUnidad = Enums.Duracion.valueOf(partialGoal.getString("duracionUnidad"));
        return new ResponsePartialGoalDTO(
                id, nombre, tipo, fecha, finalizado, duracionValor, durUnidad
        );
    }
}