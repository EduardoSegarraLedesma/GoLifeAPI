package org.GoLIfeAPI.bussiness;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.bussiness.GoalDtoMapper;
import org.GoLIfeAPI.mapper.bussiness.RecordDtoMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.GoLIfeAPI.model.record.Record;
import org.GoLIfeAPI.persistence.RecordPersistenceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecordService {

    private final GoalDtoMapper goalDtoMapper;
    private final RecordDtoMapper recordDtoMapper;
    private final RecordPersistenceController recordPersistenceController;
    private final GoalService goalService;
    private final StatsService statsService;

    @Autowired
    public RecordService(GoalDtoMapper goalDtoMapper,
                         RecordDtoMapper recordDtoMapper,
                         RecordPersistenceController recordPersistenceController,
                         GoalService goalService,
                         StatsService statsService) {
        this.goalDtoMapper = goalDtoMapper;
        this.recordDtoMapper = recordDtoMapper;
        this.recordPersistenceController = recordPersistenceController;
        this.goalService = goalService;
        this.statsService = statsService;
    }

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
                            statsService.getGoalStatsReachedBoolValueUpdateDoc(dto, bool),
                            mid),
                    bool);
        } else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de Goal inesperado: " + goal.getClass().getName());
    }

    public ResponseNumGoalDTO createNumRecord(CreateNumRecordDTO dto, String uid, String mid) {
        Goal goal = goalService.validateAndGetGoal(uid, mid);
        if (goal instanceof NumGoal num) {
            NumRecord numRecord = recordDtoMapper.mapCreateNumRecordDtoToBoolRecord(dto);
            validateRecord(num.getFecha(), numRecord, num.getRegistros());
            return goalDtoMapper.mapNumGoalToResponseNumGoalDTO(
                    recordPersistenceController.createNumRecord(
                            numRecord,
                            statsService.getGoalStatsReachedNumValueUpdateDoc(dto, num),
                            mid),
                    num);
        } else if (goal instanceof BoolGoal) {
            throw new BadRequestException("Tipo de registro incorrecto para la meta");
        } else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de Goal inesperado: " + goal.getClass().getName());
    }

    public void deleteRecord(String uid, String mid, LocalDate date) {
        Goal goal = goalService.validateAndGetGoal(uid, mid);
        List<? extends Record> records;
        if (goal instanceof NumGoal num)
            records = num.getRegistros();
        else if (goal instanceof BoolGoal bool)
            records = bool.getRegistros();
        else
            // Estado imposible: tipo de Goal desconocido
            throw new IllegalStateException("Tipo de Goal inesperado: " + goal.getClass().getName());
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