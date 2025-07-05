package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.exception.BadRequestException;
import org.GoLIfeAPI.exception.ConflictException;
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

    private final RecordPersistenceController recordPersistenceController;
    private final GoalService goalService;

    @Autowired
    public RecordService(RecordPersistenceController recordPersistenceController, GoalService goalService) {
        this.recordPersistenceController = recordPersistenceController;
        this.goalService = goalService;
    }

    public ResponseBoolGoalDTO createBoolRecord(BoolRecord record, String uid, String mid) {
        Document goalDoc = goalService.validateAndGetGoal(uid, mid);
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Bool"))
            throw new BadRequestException("Tipo de Registro incorrecto para la meta");
        validateRecord(goalDoc, record);
        return goalService.mapToResponseBoolGoalDTO(recordPersistenceController.create(record.toDocument(), mid));
    }

    public ResponseNumGoalDTO createNumRecord(NumRecord record, String uid, String mid) {
        Document goalDoc = goalService.validateAndGetGoal(uid, mid);
        if (!goalDoc.getString("tipo").equalsIgnoreCase("Num"))
            throw new BadRequestException("Tipo de Registro incorrecto para la meta");
        validateRecord(goalDoc, record);
        return goalService.mapToResponseNumGoalDTO(recordPersistenceController.create(record.toDocument(), mid));
    }

    public Object deleteRecord(String uid, String mid, LocalDate date) {
        goalService.validateAndGetGoal(uid, mid);

        return goalService.mapToResponseGoalDTO(
                recordPersistenceController.delete(mid,
                        LocalDate.parse(date.toString(), DateTimeFormatter.ISO_LOCAL_DATE).toString()));
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