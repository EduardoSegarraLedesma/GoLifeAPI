package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.GoalMapper;
import org.GoLIfeAPI.mapper.UserMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class GoalService {

    private final UserMapper userMapper;
    private final GoalMapper goalMapper;
    private final StatsService statsService;
    private final GoalPersistenceController goalPersistenceController;


    @Autowired
    public GoalService(UserMapper userMapper,
                       GoalMapper goalMapper,
                       StatsService statsService,
                       GoalPersistenceController goalPersistenceController) {
        this.userMapper = userMapper;
        this.goalMapper = goalMapper;
        this.statsService = statsService;
        this.goalPersistenceController = goalPersistenceController;
    }

    public ResponseUserDTO createBoolGoal(CreateBoolGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        BoolGoal goal = goalMapper.mapCreateBoolGoalDtoToBoolGoal(dto, uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document updatedUserDoc = goalPersistenceController.create(
                goalMapper.mapBoolGoalToBoolGoalDoc(goal),
                goalMapper.mapGoalToParcialDoc(goal),
                deltaUserStatsDoc,
                uid);
        return userMapper.mapUserDocToResponseUserDTO(updatedUserDoc);
    }

    public ResponseUserDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        NumGoal goal = goalMapper.mapCreateNumGoalDtoToNumGoal(dto, uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(+1, 0);
        Document updatedUserDoc = goalPersistenceController.create(
                goalMapper.mapNumGoalToNumGoalDoc(goal),
                goalMapper.mapGoalToParcialDoc(goal),
                deltaUserStatsDoc,
                uid);
        return userMapper.mapUserDocToResponseUserDTO(updatedUserDoc);
    }

    public Object getGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        return goalMapper.mapGoalDocToResponseGoalDTO(goalDoc, goalDoc);
    }

    public ResponseUserDTO finalizeGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getBoolean("finalizado")) throw new ConflictException("La meta ya esta finalizada");
        Document update = new Document("finalizado", true);
        Document deltaUserStats = statsService.getUpdatedUserStatsDoc(0, +1);
        Document updatedUserDoc = goalPersistenceController.updateWithUserStats(update, update, deltaUserStats, uid, mid);
        return userMapper.mapUserDocToResponseUserDTO(updatedUserDoc);
    }

    public ResponseUserDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getBoolean("finalizado"))
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Bool"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdate(dto, goalDoc);
        Document updatedUserDoc = goalPersistenceController.updateWithGoalStats(
                goalMapper.mapPatchBoolGoalDtoToDoc(dto),
                goalMapper.mapPatchGoalDtoToPartialDoc(dto),
                deltaGoalStats,
                uid, mid);
        return userMapper.mapUserDocToResponseUserDTO(updatedUserDoc);
    }

    public ResponseUserDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        if (goalDoc.getBoolean("finalizado"))
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Num"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdate(dto, goalDoc);
        Document updatedUserDoc = goalPersistenceController.updateWithGoalStats(
                goalMapper.mapPatchNumGoalDtoToDoc(dto),
                goalMapper.mapPatchGoalDtoToPartialDoc(dto),
                deltaGoalStats,
                uid, mid);
        return userMapper.mapUserDocToResponseUserDTO(updatedUserDoc);
    }

    public ResponseUserStatsDTO deleteGoal(String uid, String mid) {
        Document goalDoc = validateAndGetGoal(uid, mid);
        Document deltaUserStatsDoc;
        if (goalDoc.getBoolean("finalizado"))
            deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(-1, -1);
        else
            deltaUserStatsDoc = statsService.getUpdatedUserStatsDoc(-1, 0);
        Document updatedUserDoc = goalPersistenceController.delete(deltaUserStatsDoc, uid, mid);
        return userMapper.mapUserDocToResponseUserStatsDTO(updatedUserDoc);
    }

    public Document validateAndGetGoal(String uid, String mid) {
        Document goalDoc = goalPersistenceController.read(mid);
        if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
        if (!uid.equals(goalDoc.getString("uid")))
            throw new ForbiddenResourceException("No autorizado para acceder a esta meta");
        return goalDoc;
    }
}