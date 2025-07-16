package org.GoLIfeAPI.persistence.implementation;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.persistence.GoalDocMapper;
import org.GoLIfeAPI.mapper.persistence.RecordDocMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.GoLIfeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;
import org.GoLIfeAPI.persistence.interfaces.IRecordPersistenceController;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecordPersistenceController extends BasePersistenceController implements IRecordPersistenceController {

    private final GoalDocMapper goalDocMapper;
    private final RecordDocMapper recordDocMapper;
    private final GoalDAO goalDAO;

    @Autowired
    public RecordPersistenceController(TransactionRunner transactionRunner,
                                       GoalDocMapper goalDocMapper,
                                       RecordDocMapper recordDocMapper,
                                       GoalDAO goalDAO) {
        super(transactionRunner);
        this.goalDocMapper = goalDocMapper;
        this.recordDocMapper = recordDocMapper;
        this.goalDAO = goalDAO;
    }

    @Override
    public BoolGoal createBoolrecord(BoolRecord record, Document goalStatsUpdate, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document recordDoc = recordDocMapper.mapBoolRecordToDoc(record);
                Document goalDoc = create(session, recordDoc, goalStatsUpdate, mid);
                return goalDocMapper.mapDocToBoolGoal(goalDoc);
            });
        } catch (NotFoundException e) {
            throw new NotFoundException("Meta del registro no encontrada");
        } catch (Exception e) {
            throw new RuntimeException("Error interno al crear el registro", e);
        }
    }

    @Override
    public NumGoal createNumRecord(NumRecord record, Document goalStatsUpdate, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document recordDoc = recordDocMapper.mapNumRecordToDoc(record);
                Document goalDoc = create(session, recordDoc, goalStatsUpdate, mid);
                return goalDocMapper.mapDocToNumGoal(goalDoc);
            });
        } catch (NotFoundException e) {
            throw new NotFoundException("Meta del registro no encontrada");
        } catch (Exception e) {
            throw new RuntimeException("Error interno al crear el registro", e);
        }
    }

    private Document create(ClientSession session,
                            Document recordDoc, Document goalStatsUpdate, String mid) {
        Document goalDoc = goalDAO.insertRecordInListByGoalId(session, mid, recordDoc);
        if (goalDoc == null) throw new NotFoundException("");
        if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
            goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
            if (goalDoc == null) throw new NotFoundException("");
        }
        return goalDoc;
    }

    @Override
    public void delete(String mid, String date) {
        try {
            transactionRunner.run(session -> {
                Document goalDoc = goalDAO.removeRecordFromListByGoalIdAndDate(session, mid, date);
                if (goalDoc == null) throw new NotFoundException("Meta o Registro no encontrados");
                return null;
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al borrar el registro", e);
        }
    }
}