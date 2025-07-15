package org.GoLIfeAPI.persistence;

import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.persistence.dao.GoalDAO;
import org.GoLIfeAPI.persistence.dao.UserDAO;
import org.GoLIfeAPI.persistence.transaction.TransactionRunner;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GoalPersistenceController extends BasePersistenceController {

    private final UserDAO userDAO;
    private final GoalDAO goalDAO;

    @Autowired
    public GoalPersistenceController(TransactionRunner transactionRunner,
                                     UserDAO userDAO, GoalDAO goalDAO) {
        super(transactionRunner);
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
    }

    public Document create(Document goal, Document partialGoal, Document userStatsUpdate, String uid) {
        try {
            return transactionRunner.run(session -> {
                String id = goalDAO.insertDoc(session, goal);
                if (id == null || id.isBlank()) throw new RuntimeException();
                partialGoal.put("_id", new ObjectId(id));
                Document userDoc = userDAO.insertPartialGoalInListByUid(session, uid, partialGoal);
                if (userDoc == null) throw new RuntimeException();
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new RuntimeException();
                return userDoc;
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al crear la meta", e);
        }
    }

    public Document read(String id) {
        try {
            Document goalDoc = goalDAO.findDocById(id);
            if (goalDoc == null) throw new NotFoundException("No se ha encontrado la Meta");
            return goalDoc;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al leer la meta", e);
        }
    }

    public Document updateWithUserStats(Document goal, Document partialGoalUpdate,
                                        Document userStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goal);
                if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
                Document userDoc;
                if (partialGoalUpdate != null && !partialGoalUpdate.isEmpty()) {
                    userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                    if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                }
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDoc;
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    public Document updateWithGoalStats(Document goal, Document partialGoalUpdate,
                                        Document goalStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                session.startTransaction();
                Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goal);
                if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
                if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                    goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
                    if (goalDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                }
                Document userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDoc;
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    public Document delete(Document userStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                DeleteResult deleteGoal = goalDAO.deleteGoalByGoalId(session, mid);
                if (!deleteGoal.wasAcknowledged()) throw new RuntimeException();
                if (deleteGoal.getDeletedCount() == 0) throw new NotFoundException("Meta no encontrada");
                Document userDoc = userDAO.removePartialGoalFromListByUidAndGoalId(session, uid, mid);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDoc;
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al eliminar la meta", e);
        }
    }
}