package org.GoLIfeAPI.persistence.implementation;

import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.mapper.persistence.GoalDocMapper;
import org.GoLIfeAPI.mapper.persistence.UserDocMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.GoLIfeAPI.persistence.implementation.dao.GoalDAO;
import org.GoLIfeAPI.persistence.implementation.dao.UserDAO;
import org.GoLIfeAPI.persistence.implementation.transaction.TransactionRunner;
import org.GoLIfeAPI.persistence.interfaces.IGoalPersistenceController;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GoalPersistenceController extends BasePersistenceController implements IGoalPersistenceController {

    private final UserDocMapper userDocMapper;
    private final GoalDocMapper goalDocMapper;
    private final UserDAO userDAO;
    private final GoalDAO goalDAO;

    @Autowired
    public GoalPersistenceController(TransactionRunner transactionRunner,
                                     UserDocMapper userDocMapper,
                                     GoalDocMapper goalDocMapper,
                                     UserDAO userDAO, GoalDAO goalDAO) {
        super(transactionRunner);
        this.userDocMapper = userDocMapper;
        this.goalDocMapper = goalDocMapper;
        this.userDAO = userDAO;
        this.goalDAO = goalDAO;
    }

    @Override
    public User createBoolGoal(BoolGoal goal, Document userStatsUpdate, String uid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDocMapper.mapBoolGoalToDoc(goal);
                Document partialGoalDoc = goalDocMapper.mapGoalToPartialGoalDoc(goal);
                Document userDoc = create(session, goalDoc, partialGoalDoc, userStatsUpdate, uid);
                return userDocMapper.mapDocToUser(userDoc);
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al crear la meta", e);
        }
    }

    @Override
    public User createNumGoal(NumGoal goal, Document userStatsUpdate, String uid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDocMapper.mapNumGoalToDoc(goal);
                Document partialGoalDoc = goalDocMapper.mapGoalToPartialGoalDoc(goal);
                Document userDoc = create(session, goalDoc, partialGoalDoc, userStatsUpdate, uid);
                return userDocMapper.mapDocToUser(userDoc);
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al crear la meta", e);
        }
    }

    private Document create(ClientSession session,
                            Document goalDoc, Document partialGoalDoc, Document userStatsUpdate, String uid) {
        String id = goalDAO.insertDoc(session, goalDoc);
        if (id == null || id.isBlank()) throw new RuntimeException();
        partialGoalDoc.put("_id", new ObjectId(id));
        Document userDoc = userDAO.insertPartialGoalInListByUid(session, uid, partialGoalDoc);
        if (userDoc == null) throw new RuntimeException();
        userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
        if (userDoc == null) throw new RuntimeException();
        return userDoc;
    }

    @Override
    public Goal read(String id) {
        try {
            Document goalDoc = goalDAO.findDocById(id);
            if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
            return goalDocMapper.mapDocToGoal(goalDoc);
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al leer la meta", e);
        }
    }

    @Override
    public User updateWithUserStats(Document goalUpdate, Document partialGoalUpdate,
                                    Document userStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goalUpdate);
                if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
                Document userDoc;
                if (partialGoalUpdate != null && !partialGoalUpdate.isEmpty()) {
                    userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                    if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                }
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDocMapper.mapDocToUser(userDoc);
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    @Override
    public User updateWithGoalStats(Document goalUpdate, Document partialGoalUpdate,
                                    Document goalStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                Document goalDoc = goalDAO.updateGoalByGoalId(session, mid, goalUpdate);
                if (goalDoc == null) throw new NotFoundException("Meta no encontrada");
                if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                    goalDoc = goalDAO.updateGoalSatsByGoalId(session, mid, goalStatsUpdate);
                    if (goalDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                }
                Document userDoc = userDAO.updatePartialGoalInListByUidAndGoalId(session, uid, mid, partialGoalUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDocMapper.mapDocToUser(userDoc);
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al editar la meta", e);
        }
    }

    @Override
    public UserStats delete(Document userStatsUpdate, String uid, String mid) {
        try {
            return transactionRunner.run(session -> {
                DeleteResult deleteGoal = goalDAO.deleteGoalByGoalId(session, mid);
                if (!deleteGoal.wasAcknowledged()) throw new RuntimeException();
                if (deleteGoal.getDeletedCount() == 0) throw new NotFoundException("Meta no encontrada");
                Document userDoc = userDAO.removePartialGoalFromListByUidAndGoalId(session, uid, mid);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                userDoc = userDAO.updateUserStatsByUid(session, uid, userStatsUpdate);
                if (userDoc == null) throw new NotFoundException("Usuario de la Meta no encontrado");
                return userDocMapper.mapDocToUserStats(userDoc);
            });
        } catch (NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error interno al eliminar la meta", e);
        }
    }
}