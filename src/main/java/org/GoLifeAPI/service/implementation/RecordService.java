package org.GoLifeAPI.service.implementation;

import org.GoLifeAPI.service.interfaces.IRecordService;
import org.GoLifeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLifeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLifeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLifeAPI.dto.record.CreateNumRecordDTO;
import org.GoLifeAPI.exception.BadRequestException;
import org.GoLifeAPI.exception.ConflictException;
import org.GoLifeAPI.exception.NotFoundException;
import org.GoLifeAPI.mapper.service.GoalDtoMapper;
import org.GoLifeAPI.mapper.service.RecordDtoMapper;
import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.Goal;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.model.record.BoolRecord;
import org.GoLifeAPI.model.record.NumRecord;
import org.GoLifeAPI.model.record.Record;
import org.GoLifeAPI.persistence.interfaces.IRecordPersistenceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecordService implements IRecordService {

    private final GoalDtoMapper goalDtoMapper;
    private final RecordDtoMapper recordDtoMapper;
    private final IRecordPersistenceController recordPersistenceController;
    private final GoalService goalService;
    private final StatsService statsService;

    @Autowired
    public RecordService(GoalDtoMapper goalDtoMapper,
                         RecordDtoMapper recordDtoMapper,
                         IRecordPersistenceController recordPersistenceController,
                         GoalService goalService,
                         StatsService statsService) {
        this.goalDtoMapper = goalDtoMapper;
        this.recordDtoMapper = recordDtoMapper;
        this.recordPersistenceController = recordPersistenceController;
        this.goalService = goalService;
        this.statsService = statsService;
    }

    @Override
    public ResponseBoolGoalDTO createBoolRecord(CreateBoolRecordDTO dto, String uid, String mid) {
        Goal goal = goalService.validateAndGetGoal(uid, mid);
        if (goal instanceof NumGoal) {
            throw new BadRequestException("Tipo de registro incorrecto para la meta");
        } else if (goal instanceof BoolGoal bool) {
            BoolRecord boolRecord = recordDtoMapper.mapCreateBoolRecordDtoToBoolRecord(dto);
            validateRecord(bool.getFecha(), boolRecord, bool.getRegistros());
            return goalDtoMapper.mapBoolGoalToResponseBoolGoalDTO(
                    recordPersistenceController.createBoolrecord(
                            boolRecord,
                            statsService.getGoalStatsHasFirstRecordUpdateDoc(bool),
                            mid),
                    bool);
        } else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de meta inesperada: " + goal.getClass().getName());
    }

    @Override
    public ResponseNumGoalDTO createNumRecord(CreateNumRecordDTO dto, String uid, String mid) {
        Goal goal = goalService.validateAndGetGoal(uid, mid);
        if (goal instanceof NumGoal num) {
            NumRecord numRecord = recordDtoMapper.mapCreateNumRecordDtoToBoolRecord(dto);
            validateRecord(num.getFecha(), numRecord, num.getRegistros());
            return goalDtoMapper.mapNumGoalToResponseNumGoalDTO(
                    recordPersistenceController.createNumRecord(
                            numRecord,
                            statsService.getGoalStatsHasFirstRecordUpdateDoc(num),
                            mid),
                    num);
        } else if (goal instanceof BoolGoal) {
            throw new BadRequestException("Tipo de registro incorrecto para la meta");
        } else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de meta inesperada: " + goal.getClass().getName());
    }

    @Override
    public void deleteRecord(String uid, String mid, LocalDate date) {
        Goal goal = goalService.validateAndGetGoal(uid, mid);
        List<? extends Record> records;
        if (goal instanceof NumGoal num)
            records = num.getRegistros();
        else if (goal instanceof BoolGoal bool)
            records = bool.getRegistros();
        else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de meta inesperada: " + goal.getClass().getName());
        date = LocalDate.parse(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        LocalDate finalDate = date;
        boolean exists = records.stream()
                .anyMatch(r -> r.getFecha().equals(finalDate));
        if (!exists) throw new NotFoundException("No existe un registro en esa fecha");
        recordPersistenceController.delete(mid,
                LocalDate.parse(date.toString(), DateTimeFormatter.ISO_LOCAL_DATE).toString());
    }

    private void validateRecord(LocalDate goalDate, Record record, List<? extends Record> records) {
        goalDate = LocalDate.parse(goalDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (record.getFecha().isBefore(goalDate))
            throw new BadRequestException("La fecha del registro no puede ser anterior a la fecha inicial de la meta");
        boolean exists = records.stream()
                .anyMatch(r -> r.getFecha().equals(record.getFecha()));
        if (exists) throw new ConflictException("Ya existe un registro en esa fecha");
    }
}