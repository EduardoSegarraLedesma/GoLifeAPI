package org.GoLIfeAPI.persistence;

import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.GoLIfeAPI.persistence.dao.GoalDAO;
import org.GoLIfeAPI.persistence.dao.UserDAO;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GoalPersistenceController extends BasePersistenceController {

    private final UserDAO userDAO;
    private final GoalDAO goalDAO;
    protected static final String STATS_NAME = "estadisticas";

    @Autowired
    public GoalPersistenceController(MongoService mongoService,
                                     UserDAO userDAO, GoalDAO goalDAO) {
        super(mongoService);
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
    }

    public Document create(Document goal, Document partialGoal, Document userStatsUpdate, String uid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            String objectId = goalDAO.insertDoc(session, goal);
            if (objectId != null && !objectId.isBlank()) {
                partialGoal.put("_id", new ObjectId(objectId));
                Document userDoc = userDAO.insertPartialGoalInListByUid(session, uid, partialGoal);
                if (userDoc == null) throw new Exception();
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new Exception();
                session.commitTransaction();
                session.close();
                Document composedDoc = new Document();
                composedDoc.append(STATS_NAME, userDoc.get(STATS_NAME, Document.class));
                composedDoc.append("meta", goalDAO.findDocById(objectId));
                return composedDoc;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al crear la meta", e);
        }
    }

    public Document read(String id) {
        try {
            return goalDAO.findDocById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error interno al leer la meta", e);
        }
    }

    public Document updateWithUserStats(Document goal, Document partialGoalUpdate,
                                        Document userStatsUpdate, String uid, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            // Goal
            Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goal);
            if (goalDoc == null) throw new NotFoundException("");
            // Partial Goal in User
            Document userDoc;
            if (partialGoalUpdate != null && !partialGoalUpdate.isEmpty()) {
                userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                if (userDoc == null) throw new NotFoundException("");
            }
            // User Stats in User
            userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
            if (userDoc == null) throw new Exception();
            Document composedDoc = new Document();
            composedDoc.append(STATS_NAME, userDoc.get(STATS_NAME, Document.class));
            composedDoc.append("meta", goalDoc);
            session.commitTransaction();
            session.close();
            return composedDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    public Document updateWithGoalStats(Document goal, Document partialGoalUpdate,
                                        Document goalStatsUpdate, String uid, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            // Goal
            Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goal);
            if (goalDoc == null) throw new NotFoundException("");
            // Goal Stats in Goal
            if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
                if (goalDoc == null) throw new NotFoundException("");
            }
            // Partial Goal in User
            if (partialGoalUpdate != null && !partialGoalUpdate.isEmpty()) {
                Document userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                if (userDoc == null) throw new NotFoundException("");
            }
            session.commitTransaction();
            session.close();
            return goalDoc;
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    public Document delete(Document userStatsUpdate, String uid, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            DeleteResult deleteGoal = goalDAO.deleteGoalByGoalId(session, mid);
            if (!deleteGoal.wasAcknowledged()) throw new Exception();
            if (deleteGoal.getDeletedCount() == 0) throw new NotFoundException("");
            Document userDoc = userDAO.removePartialGoalFromListByUidAndGoalId(session, uid, mid);
            if (userDoc == null) throw new NotFoundException("");
            userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
            if (userDoc == null) throw new Exception();
            session.commitTransaction();
            session.close();
            return new Document(STATS_NAME, userDoc.get(STATS_NAME, Document.class));
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            // "Error interno al eliminar la meta"
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}