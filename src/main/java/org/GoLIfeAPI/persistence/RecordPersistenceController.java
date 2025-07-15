package org.GoLIfeAPI.persistence;

import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.persistence.dao.GoalDAO;
import org.GoLIfeAPI.persistence.transaction.TransactionRunner;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecordPersistenceController extends BasePersistenceController {

    private final GoalDAO goalDAO;

    @Autowired
    public RecordPersistenceController(TransactionRunner transactionRunner,
                                       GoalDAO goalDAO) {
        super(transactionRunner);
        this.goalDAO = goalDAO;
    }

    public Document create(Document record, Document goalStatsUpdate, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDAO.insertRecordInListByGoalId(session, mid, record);
                if (goalDoc == null) throw new NotFoundException("");
                if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                    goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
                    if (goalDoc == null) throw new NotFoundException("");
                }
                return goalDoc;
            });
        } catch (NotFoundException e) {
            throw new NotFoundException("Meta del registro no encontrada");
        } catch (Exception e) {
            throw new RuntimeException("Error interno al crear el registro", e);
        }
    }

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