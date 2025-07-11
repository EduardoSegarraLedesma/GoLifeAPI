package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.GoalMapper;
import org.GoLIfeAPI.mapper.RecordMapper;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.GoLIfeAPI.model.record.Record;
import org.GoLIfeAPI.persistence.RecordPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecordService {

    private final GoalMapper goalMapper;
    private final RecordMapper recordMapper;
    private final RecordPersistenceController recordPersistenceController;
    private final GoalService goalService;
    private final StatsService statsService;

    @Autowired
    public RecordService(GoalMapper goalMapper,
                         RecordMapper recordMapper,
                         RecordPersistenceController recordPersistenceController,
                         GoalService goalService,
                         StatsService statsService) {
        this.goalMapper = goalMapper;
        this.recordMapper = recordMapper;
        this.recordPersistenceController = recordPersistenceController;
        this.goalService = goalService;
        this.statsService = statsService;
    }

    public ResponseBoolGoalDTO createBoolRecord(CreateBoolRecordDTO dto, String uid, String mid) {
        Document goalDoc = goalService.validateAndGetGoal(uid, mid);
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Bool"))
            throw new BadRequestException("Tipo de registro incorrecto para la meta");
        BoolRecord boolRecord = recordMapper.mapCreateBoolRecordDtoToBoolRecord(dto);
        validateRecord(goalDoc, boolRecord);
        return goalMapper.mapGoalDocToResponseBoolGoalDTO(
                recordPersistenceController.create(
                        recordMapper.mapBoolRecordToBoolRecordDoc(boolRecord),
                        statsService.getGoalStatsReachedBoolValueUpdate(dto, goalDoc),
                        mid),
                goalDoc);
    }

    public ResponseNumGoalDTO createNumRecord(CreateNumRecordDTO dto, String uid, String mid) {
        Document goalDoc = goalService.validateAndGetGoal(uid, mid);
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Num"))
            throw new BadRequestException("Tipo de Registro incorrecto para la meta");
        NumRecord numRecord = recordMapper.mapCreateNumRecordDtoToBoolRecord(dto);
        validateRecord(goalDoc, numRecord);
        return goalMapper.mapGoalDocToResponseNumGoalDTO(
                recordPersistenceController.create(
                        recordMapper.mapNumRecordToNumRecordDoc(numRecord),
                        statsService.getGoalStatsReachedNumValueUpdate(dto, goalDoc),
                        mid),
                goalDoc);
    }

    public void deleteRecord(String uid, String mid, LocalDate date) {
        Document goalDoc = goalService.validateAndGetGoal(uid, mid);
        List<Document> records = (List<Document>) goalDoc.get("registros");
        String stringDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        boolean exists = records.stream().anyMatch(reg -> stringDate.equals(reg.getString("fecha")));
        if (!exists) throw new NotFoundException("No existe un registro en esa fecha");
        recordPersistenceController.delete(mid,
                LocalDate.parse(date.toString(), DateTimeFormatter.ISO_LOCAL_DATE).toString());
    }

    private void validateRecord(Document goalDoc, Record record) {
        LocalDate goalDate = LocalDate.parse(goalDoc.getString("fecha"), DateTimeFormatter.ISO_LOCAL_DATE);
        if (record.getFecha().isBefore(goalDate))
            throw new BadRequestException("La fecha del registro no puede ser anterior a la fecha inicial de la meta");
        List<Document> records = (List<Document>) goalDoc.get("registros");
        String newDate = record.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE);
        boolean exists = records.stream().anyMatch(reg -> newDate.equals(reg.getString("fecha")));
        if (exists) throw new ConflictException("Ya existe un registro en esa fecha");
    }
}