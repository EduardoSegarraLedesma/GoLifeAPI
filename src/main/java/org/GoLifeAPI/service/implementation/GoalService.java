package org.GoLifeAPI.service.implementation;

import org.GoLifeAPI.infrastructure.KeyManagementService;
import org.GoLifeAPI.service.interfaces.IGoalService;
import org.GoLifeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLifeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLifeAPI.exception.BadRequestException;
import org.GoLifeAPI.exception.ConflictException;
import org.GoLifeAPI.exception.ForbiddenResourceException;
import org.GoLifeAPI.mapper.service.GoalDtoMapper;
import org.GoLifeAPI.mapper.service.GoalPatchMapper;
import org.GoLifeAPI.mapper.service.UserDtoMapper;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.Goal;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.persistence.interfaces.IGoalPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class GoalService implements IGoalService {

    private final UserDtoMapper userDtoMapper;
    private final GoalDtoMapper goalDtoMapper;
    private final GoalPatchMapper goalPatchMapper;
    private final StatsService statsService;
    private final KeyManagementService keyManagementService;
    private final IGoalPersistenceController goalPersistenceController;

    @Autowired
    public GoalService(UserDtoMapper userDtoMapper,
                       GoalDtoMapper goalDtoMapper,
                       GoalPatchMapper goalPatchMapper,
                       StatsService statsService,
                       KeyManagementService keyManagementService,
                       IGoalPersistenceController goalPersistenceController) {
        this.userDtoMapper = userDtoMapper;
        this.goalDtoMapper = goalDtoMapper;
        this.goalPatchMapper = goalPatchMapper;
        this.statsService = statsService;
        this.keyManagementService = keyManagementService;
        this.goalPersistenceController = goalPersistenceController;
    }

    @Override
    public ResponseUserDTO createBoolGoal(CreateBoolGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        String signedUid = keyManagementService.sign(uid);
        BoolGoal goal = goalDtoMapper.mapCreateBoolGoalDtoToBoolGoal(dto, signedUid, finalDate);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.createBoolGoal(
                        goal,
                        statsService.getUserStatsUpdateDoc(+1, 0),
                        signedUid));
    }

    @Override
    public ResponseUserDTO createNumGoal(CreateNumGoalDTO dto, String uid) {
        LocalDate finalDate = statsService.calculateFinalGoalDate(
                dto.getFecha(),
                dto.getDuracionValor(),
                dto.getDuracionUnidad());
        String signedUid = keyManagementService.sign(uid);
        NumGoal goal = goalDtoMapper.mapCreateNumGoalDtoToNumGoal(dto, signedUid, finalDate);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.createNumGoal(
                        goal,
                        statsService.getUserStatsUpdateDoc(+1, 0),
                        signedUid));
    }

    @Override
    public Object getGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal instanceof NumGoal num)
            return goalDtoMapper.mapNumGoalToResponseNumGoalDTO(num, num);
        else if (goal instanceof BoolGoal bool)
            return goalDtoMapper.mapBoolGoalToResponseBoolGoalDTO(bool, bool);
        else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de Meta inesperada: " + goal.getClass().getName());
    }

    @Override
    public ResponseUserDTO finalizeGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada");
        Document update = goalPatchMapper.mapFinalizePatchToDoc();
        String signedUid = keyManagementService.sign(uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithUserStats(
                        update, update,
                        statsService.getUserStatsUpdateDoc(0, +1),
                        signedUid, mid));
    }

    @Override
    public ResponseUserDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goal.getTipo().toString().equalsIgnoreCase("Bool"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        String signedUid = keyManagementService.sign(uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithGoalStats(
                        goalPatchMapper.mapPatchBoolGoalDtoToDoc(dto),
                        goalPatchMapper.mapPatchGoalDtoToPartialDoc(dto),
                        statsService.getGoalStatsFinalDateUpdateDoc(dto, goal),
                        signedUid, mid));
    }

    @Override
    public ResponseUserDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        if (goal.getFinalizado())
            throw new ConflictException("La meta ya esta finalizada, no puedes modificarla");
        if (!goal.getTipo().toString().equalsIgnoreCase("Num"))
            throw new BadRequestException("Tipo incorrecto para la meta");
        String signedUid = keyManagementService.sign(uid);
        return userDtoMapper.mapUserToResponseUserDTO(
                goalPersistenceController.updateWithGoalStats(
                        goalPatchMapper.mapPatchNumGoalDtoToDoc(dto),
                        goalPatchMapper.mapPatchGoalDtoToPartialDoc(dto),
                        statsService.getGoalStatsFinalDateUpdateDoc(dto, goal),
                        signedUid, mid));
    }

    @Override
    public ResponseUserStatsDTO deleteGoal(String uid, String mid) {
        Goal goal = validateAndGetGoal(uid, mid);
        Document deltaUserStatsDoc;
        if (goal.getFinalizado())
            deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(-1, -1);
        else
            deltaUserStatsDoc = statsService.getUserStatsUpdateDoc(-1, 0);
        String signedUid = keyManagementService.sign(uid);
        return userDtoMapper.mapUserStatsToResponseUserStatsDTO(
                goalPersistenceController.delete(
                        deltaUserStatsDoc,
                        signedUid, mid));
    }

    public Goal validateAndGetGoal(String uid, String mid) {
        Goal goal = goalPersistenceController.read(mid);
        if(!keyManagementService.verify(uid,goal.getUid()))
            throw new ForbiddenResourceException("No autorizado para acceder a esta meta");
        return goal;
    }
}