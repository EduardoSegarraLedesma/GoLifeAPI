package org.GoLIfeAPI.persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.GoLIfeAPI.persistence.dao.GoalDAO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RecordPersistenceController extends BasePersistenceController {

    private final GoalDAO goalDAO;

    @Autowired
    public RecordPersistenceController(MongoService mongoService,
                                       GoalDAO goalDAO) {
        super(mongoService);
        this.goalDAO = goalDAO;
    }

    public Document create(Document record, Document goalStatsUpdate, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            Document goalDoc = goalDAO.insertRecordInListByGoalId(session, mid, record);
            if (goalDoc == null) throw new NotFoundException("");
            if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
                if (goalDoc == null) throw new NotFoundException("");
            }
            session.commitTransaction();
            session.close();
            return goalDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta del registro no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al crear el registro", e);
        }
    }

    public void delete(String mid, String date) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            Document goalDoc = goalDAO.removeRecordFromListByGoalIdAndDate(session, mid, date);
            if (goalDoc == null) throw new NotFoundException("");
            session.commitTransaction();
            session.close();
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Registro no encontrado");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al borrar el registro", e);
        }
    }
}