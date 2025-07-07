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
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        Goal goal = dto.toEntity(uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document composedDoc = goalPersistenceController.create(
                goal.toDocument(),
                goal.toParcialDocument(),
                deltaUserStatsDoc,
                uid);
        return new ResponseBoolGoalUserStatsDTO(
                mapToResponseBoolGoalDTO(
                        composedDoc.get("meta", Document.class),
                        composedDoc.get("meta", Document.class)),
                statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
    }

    public ResponseNumGoalUserStatsDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        Goal goal = dto.toEntity(uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document composedDoc = goalPersistenceController.create(
                goal.toDocument(),
                goal.toParcialDocument(),
                deltaUserStatsDoc,
                uid);
        return new ResponseNumGoalUserStatsDTO(
                mapToResponseNumGoalDTO(
                        composedDoc.get("meta", Document.class),
                        composedDoc.get("meta", Document.class)),
                statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
    }

    public Object getGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        return mapToResponseGoalDTO(goalDoc, goalDoc);
    }

    public Object finalizeGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getBoolean("finalizado")) throw new ConflictException("La meta ya esta finalizada");
        Document update = new Document("finalizado", true);
        Document deltaUserStats = statsService.getUpdatedUserStatsDoc(0, +1);
        Document composedDoc = goalPersistenceController.update(update, null, update, deltaUserStats, uid, mid);
        return mapToComposedResponseGoalDTO(composedDoc, goalDoc);
    }

    public ResponseBoolGoalDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Bool")) {
            Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdate(dto, goalDoc);
            return mapToResponseBoolGoalDTO(
                    goalPersistenceController.update(
                            dto.toDocument(), deltaGoalStats,
                            dto.toParcialDocument(), null, uid, mid),
                    goalDoc);
        } else throw new BadRequestException("Tipo incorrecto para la meta");
    }

    public ResponseNumGoalDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getString("tipo").equalsIgnoreCase("Num")) {
            Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdate(dto, goalDoc);
            return mapToResponseNumGoalDTO(
                    goalPersistenceController.update(
                            dto.toDocument(), deltaGoalStats,
                            dto.toParcialDocument(), null, uid, mid),
                    goalDoc);
        } else throw new BadRequestException("Tipo incorrecto para la meta");
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

    public Object mapToComposedResponseGoalDTO(Document composedDoc, Document oldDoc) {
        Document newDoc = composedDoc.get("meta", Document.class);
        String type = newDoc.getString("tipo");
        if (type.equals("Bool"))
            return new ResponseBoolGoalUserStatsDTO(
                    mapToResponseBoolGoalDTO(newDoc, oldDoc),
                    statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
        else if (type.equals("Num"))
            return new ResponseNumGoalUserStatsDTO(
                    mapToResponseNumGoalDTO(newDoc, oldDoc),
                    statsService.mapEmbeddedToResponseUserStatsDTO(composedDoc));
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public Object mapToResponseGoalDTO(Document newDoc, Document oldDoc) {
        String type = newDoc.getString("tipo");
        if (type.equals("Bool"))
            return mapToResponseBoolGoalDTO(newDoc, oldDoc);
        else if (type.equals("Num"))
            return mapToResponseNumGoalDTO(newDoc, oldDoc);
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public ResponseBoolGoalDTO mapToResponseBoolGoalDTO(Document newDoc, Document oldDoc) {
        List<Document> boolRecords = newDoc.getList("registros", Document.class);
        List<ResponseBoolRecordDTO> registros = Collections.emptyList();
        if (boolRecords != null) {
            registros = boolRecords.stream().map(d -> {
                return new ResponseBoolRecordDTO(
                        d.getBoolean("valorBool"),
                        d.getString("fecha"));
            }).collect(Collectors.toList());
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
                statsService.mapEmbeddedToResponseGoalStatsDTO(newDoc, oldDoc),
                registros
        );
    }

    public ResponseNumGoalDTO mapToResponseNumGoalDTO(Document newDoc, Document oldDoc) {
        List<Document> numRecords = newDoc.getList("registros", Document.class);
        List<ResponseNumRecordDTO> registros = Collections.emptyList();
        if (numRecords != null) {
            registros = numRecords.stream().map(d -> {
                return new ResponseNumRecordDTO(
                        d.getDouble("valorNum"),
                        d.getString("fecha"));
            }).collect(Collectors.toList());
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
                statsService.mapEmbeddedToResponseGoalStatsDTO(newDoc, oldDoc),
                registros,
                newDoc.getDouble("valorObjetivo"),
                newDoc.getString("unidad")
        );
    }
}