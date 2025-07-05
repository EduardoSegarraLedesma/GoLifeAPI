package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.GoLIfeAPI.persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalPersistenceController goalPersistenceController;

    @Autowired
    public GoalService(GoalPersistenceController goalPersistenceController) {
        this.goalPersistenceController = goalPersistenceController;
    }

    public ResponseBoolGoalDTO createBoolGoal(CreateBoolGoalDTO dto, String uid) {
        Goal goal = dto.toEntity(uid);
        System.out.println(goal.getFinalizado());
        Document goalDoc = goalPersistenceController.create(goal.toDocument(), goal.toParcialDocument(), uid);
        return mapToResponseBoolGoalDTO(goalDoc);
    }

    public ResponseNumGoalDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        Goal goal = dto.toEntity(uid);
        Document goalDoc = goalPersistenceController.create(goal.toDocument(), goal.toParcialDocument(), uid);
        return mapToResponseNumGoalDTO(goalDoc);
    }

    public Object getGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        return mapToResponseGoalDTO(goalDoc);
    }

    public Object finalizeGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        Boolean finalized = goalDoc.getBoolean("finalizado");
        if (finalized) throw new ConflictException("La meta ya esta finalizada");
        Document update = new Document("finalizado", true);
        return mapToResponseGoalDTO(goalPersistenceController.update(update,
                update, uid, mid));
    }

    public ResponseBoolGoalDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Bool"))
            return mapToResponseBoolGoalDTO(goalPersistenceController.update(dto.toDocument(),
                    dto.toParcialDocument(), uid, mid));
        else throw new BadRequestException("Tipo incorrecto para la meta");
    }

    public ResponseNumGoalDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Num"))
            return mapToResponseNumGoalDTO(goalPersistenceController.update(dto.toDocument(),
                    dto.toParcialDocument(), uid, mid));
        else throw new BadRequestException("Tipo incorrecto para la meta");
    }

    public void deleteGoal(String uid, String mid) {
        goalPersistenceController.delete(uid, mid);
    }

    public Document validateAndGetGoal(String uid, String mid) {
        Document goalDoc = goalPersistenceController.read(mid);
        if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
        if (!uid.equals(goalDoc.getString("uid")))
            throw new ForbiddenResourceException("No autorizado para acceder a esta meta");
        return goalDoc;
    }

    public Object mapToResponseGoalDTO(Document doc) {
        String type = doc.getString("tipo");
        if (type.equals("Bool"))
            return mapToResponseBoolGoalDTO(doc);
        else if (type.equals("Num"))
            return mapToResponseNumGoalDTO(doc);
        else
            return null;
    }

    public ResponseBoolGoalDTO mapToResponseBoolGoalDTO(Document doc) {
        List<Document> boolRecords = doc.getList("registros", Document.class);
        List<BoolRecord> registros = Collections.emptyList();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        if (boolRecords != null) {
            registros = boolRecords.stream().map(d -> {
                String fechaStr = d.getString("fecha");
                LocalDate fecha = LocalDate.parse(fechaStr, fmt);
                Boolean valorBool = d.getBoolean("valorBool");
                return new BoolRecord(valorBool, fecha);
            }).collect(Collectors.toList());
        }
        return new ResponseBoolGoalDTO(
                doc.getObjectId("_id").toString(),
                doc.getString("nombre"),
                Goal.Tipo.valueOf(doc.getString("tipo")),
                doc.getString("descripcion"),
                doc.getString("fecha"),
                doc.getBoolean("finalizado"),
                doc.getInteger("duracionValor"),
                Goal.Duracion.valueOf(doc.getString("duracionUnidad")),
                registros
        );
    }

    public ResponseNumGoalDTO mapToResponseNumGoalDTO(Document doc) {
        List<Document> numRecords = doc.getList("registros", Document.class);
        List<NumRecord> registros = Collections.emptyList();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        if (numRecords != null) {
            registros = numRecords.stream().map(d -> {
                String fechaStr = d.getString("fecha");
                LocalDate fecha = LocalDate.parse(fechaStr, fmt);
                double valorNumDouble = doc.getDouble("valorNum");
                Float valorNum = (float) valorNumDouble;
                return new NumRecord(valorNum, fecha);
            }).collect(Collectors.toList());
        }
        double valorObjetivoDouble = doc.getDouble("valorObjetivo");
        Float valorObjetivo = (float) valorObjetivoDouble;
        return new ResponseNumGoalDTO(
                doc.getObjectId("_id").toString(),
                doc.getString("nombre"),
                Goal.Tipo.valueOf(doc.getString("tipo")),
                doc.getString("descripcion"),
                doc.getString("fecha"),
                doc.getBoolean("finalizado"),
                doc.getInteger("duracionValor"),
                Goal.Duracion.valueOf(doc.getString("duracionUnidad")),
                registros,
                valorObjetivo,
                doc.getString("unidad")
        );
    }
}