package org.GoLIfeAPI.bussiness;

import org.GoLIfeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.ForbiddenResourceException;
import org.GoLIfeAPI.mapper.bussiness.GoalDtoMapper;
import org.GoLIfeAPI.mapper.bussiness.GoalPatchMapper;
import org.GoLIfeAPI.mapper.bussiness.UserDtoMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.persistence.GoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class GoalService {

    private final UserDtoMapper userDtoMapper;
    private final GoalDtoMapper goalDtoMapper;
    private final GoalPatchMapper goalPatchMapper;
    private final StatsService statsService;
    private final GoalPersistenceController goalPersistenceController;

    @Autowired
    public GoalService(UserDtoMapper userDtoMapper,
                       GoalDtoMapper goalDtoMapper,
                       GoalPatchMapper goalPatchMapper,
                       StatsService statsService,
                       GoalPersistenceController goalPersistenceController) {
        this.userDtoMapper = userDtoMapper;
        this.goalDtoMapper = goalDtoMapper;
        this.goalPatchMapper = goalPatchMapper;
        this.statsService = statsService;
        this.goalPersistenceController = goalPersistenceController;
    }

    public ResponseUserDTO createBoolGoal(CreateBoolGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        BoolGoal goal = goalDtoMapper.mapCreateBoolGoalDtoToBoolGoal(dto, uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(+1, 0);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.createBoolGoal(
                        goal, deltaUserStatsDoc, uid));
    }

    public ResponseUserDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        NumGoal goal = goalDtoMapper.mapCreateNumGoalDtoToNumGoal(dto, uid, finalDate);
        Document deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(+1, 0);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.createNumGoal(
                        goal, deltaUserStatsDoc, uid));
    }

    public Object getGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal instanceof NumGoal num)
            return goalDtoMapper.mapNumGoalToResponseNumGoalDTO(num, num);
        else if (goal instanceof BoolGoal bool)
            return goalDtoMapper.mapBoolGoalToResponseBoolGoalDTO(bool, bool);
        else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de Goal inesperado: " + goal.getClass().getName());
    }

    public ResponseUserDTO finalizeGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada");
        Document update = new Document("finalizado", true);
        Document deltaUserStats = statsService.getUserStatsUpdateDoc(0, +1);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithUserStats(
                        update, update, deltaUserStats, uid, mid));
    }

    public ResponseUserDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goal.getTipo().toString().equalsIgnoreCase("Bool"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithGoalStats(
                        goalPatchMapper.mapPatchBoolGoalDtoToDoc(dto),
                        goalPatchMapper.mapPatchGoalDtoToPartialDoc(dto),
                        deltaGoalStats,
                        uid, mid));
    }

    public ResponseUserDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goal.getTipo().toString().equalsIgnoreCase("Num"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        Document deltaGoalStats = statsService.getGoalStatsFinalDateUpdateDoc(dto, goal);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithGoalStats(
                        goalPatchMapper.mapPatchNumGoalDtoToDoc(dto),
                        goalPatchMapper.mapPatchGoalDtoToPartialDoc(dto),
                        deltaGoalStats,
                        uid, mid));
    }

    public ResponseUserStatsDTO deleteGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        Document deltaUserStatsDoc;
        if (goal.getFinalizado())
            deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(-1, -1);
        else
            deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(-1, 0);
        return userDtoMapper.mapUserStatsToResponseUserStatsDTO(
                goalPersistenceController.delete(
                        deltaUserStatsDoc, uid, mid));
    }

    public Goal validateAndGetGoal(String uid, String mid) {
        Goal goal = goalPersistenceController.readGoal(mid);
        if (!uid.equals(goal.getUid()))
            throw new ForbiddenResourceException("No autorizado para acceder a esta meta");
        return goal;
    }
}