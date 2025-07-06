package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.composed.ResponseBoolGoalUserStatsDTO;
import org.GoLIfeAPI.dto.composed.ResponseNumGoalUserStatsDTO;
import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalPersistenceController goalPersistenceController;
    private final StatsService statsService;

    @Autowired
    public GoalService(GoalPersistenceController goalPersistenceController, StatsService statsService) {
        this.goalPersistenceController = goalPersistenceController;
        this.statsService = statsService;
    }

    public ResponseBoolGoalUserStatsDTO createBoolGoal(CreateBoolGoalDTO dto, String uid) {
        Goal goal = dto.toEntity(uid);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document composedDoc = goalPersistenceController.create(
                goal.toDocument(),
                goal.toParcialDocument(),
                deltaUserStatsDoc,
                uid);
        return new ResponseBoolGoalUserStatsDTO(
                mapToResponseBoolGoalDTO(composedDoc.get("meta", Document.class)),
                statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
    }

    public ResponseNumGoalUserStatsDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        Goal goal = dto.toEntity(uid);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document composedDoc = goalPersistenceController.create(
                goal.toDocument(),
                goal.toParcialDocument(),
                deltaUserStatsDoc,
                uid);
        return new ResponseNumGoalUserStatsDTO(
                mapToResponseNumGoalDTO(composedDoc.get("meta", Document.class)),
                statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
    }

    public Object getGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        return mapToResponseGoalDTO(goalDoc);
    }

    public Object finalizeGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getBoolean("finalizado")) throw new ConflictException("La meta ya esta finalizada");
        Document update = new Document("finalizado", true);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(0, +1);
        Document composedDoc = goalPersistenceController.update(update, update, deltaUserStatsDoc, uid, mid);
        return mapToComposedResponseGoalDTO(composedDoc);
    }

    public ResponseBoolGoalDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Bool"))
            return mapToResponseBoolGoalDTO(goalPersistenceController.update(dto.toDocument(),
                    dto.toParcialDocument(), null, uid, mid));
        else throw new BadRequestException("Tipo incorrecto para la meta");
    }

    public ResponseNumGoalDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Num"))
            return mapToResponseNumGoalDTO(goalPersistenceController.update(dto.toDocument(),
                    dto.toParcialDocument(), null, uid, mid));
        else throw new BadRequestException("Tipo incorrecto para la meta");
    }

    public ResponseUserStatsDTO deleteGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        Document deltaUserStatsDoc;
        if (goalDoc.getBoolean("finalizado"))
            deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(-1, -1);
        else
            deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(-1, 0);
        Document userStatsDoc = goalPersistenceController.delete(deltaUserStatsDoc, uid, mid);
        return statsService.mapToResponseUserStatsDTO(userStatsDoc);
    }

    public Document validateAndGetGoal(String uid, String mid) {
        Document goalDoc = goalPersistenceController.read(mid);
        if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
        if (!uid.equals(goalDoc.getString("uid")))
            throw new ForbiddenResourceException("No autorizado para acceder a esta meta");
        return goalDoc;
    }

    public Object mapToComposedResponseGoalDTO(Document composedDoc) {
        Document goalDoc = composedDoc.get("meta", Document.class);
        String type = goalDoc.getString("tipo");
        if (type.equals("Bool"))
            return new ResponseBoolGoalUserStatsDTO(
                    mapToResponseBoolGoalDTO(goalDoc),
                    statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
        else if (type.equals("Num"))
            return new ResponseNumGoalUserStatsDTO(
                    mapToResponseNumGoalDTO(goalDoc),
                    statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public Object mapToResponseGoalDTO(Document doc) {
        String type = doc.getString("tipo");
        if (type.equals("Bool"))
            return mapToResponseBoolGoalDTO(doc);
        else if (type.equals("Num"))
            return mapToResponseNumGoalDTO(doc);
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public ResponseBoolGoalDTO mapToResponseBoolGoalDTO(Document doc) {
        List<Document> boolRecords = doc.getList("registros", Document.class);
        List<ResponseBoolRecordDTO> registros = Collections.emptyList();
        if (boolRecords != null) {
            registros = boolRecords.stream().map(d -> {
                String fecha = d.getString("fecha");
                Boolean valorBool = d.getBoolean("valorBool");
                return new ResponseBoolRecordDTO(valorBool, fecha);
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
        List<ResponseNumRecordDTO> registros = Collections.emptyList();
        if (numRecords != null) {
            registros = numRecords.stream().map(d -> {
                String fecha = d.getString("fecha");
                double valorNumDouble = doc.getDouble("valorNum");
                Float valorNum = (float) valorNumDouble;
                return new ResponseNumRecordDTO(valorNum, fecha);
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