package org.GoLIfeAPI.persistence;

import com.mongodb.client.ClientSession;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GoalPersistenceController extends BasePersistenceController {

    @Autowired
    public GoalPersistenceController(MongoService mongoService) {
        super(mongoService);
    }

    public Document create(Document goal, Document partialGoal, Document userStatsUpdate, String uid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            String objectId = mongoService.insertDoc(session, goal, GOAL_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                partialGoal.put("_id", new ObjectId(objectId));
                Document userDoc = mongoService.insertEmbeddedDocInListByParentKey(session, USER_ID_NAME, uid, USER_COLLECTION_NAME,
                        GOAL_LIST_NAME, partialGoal);
                if (userDoc == null) throw new Exception();
                userDoc = mongoService.updateIncEmbeddedDocByParentKey(session, USER_ID_NAME, uid, USER_COLLECTION_NAME,
                        STATS_NAME, userStatsUpdate);
                if (userDoc == null) throw new Exception();
                session.commitTransaction();
                session.close();
                Document composedDoc = new Document();
                composedDoc.append(STATS_NAME, userDoc.get(STATS_NAME, Document.class));
                composedDoc.append("meta", mongoService.findDocById(objectId, GOAL_COLLECTION_NAME));
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
            return mongoService.findDocById(id, GOAL_COLLECTION_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Error interno al leer la meta", e);
        }
    }

    public Document update(Document goal, Document goalStatsUpdate,
                           Document partialGoalUpdate, Document userStatsUpdate, String uid, String mid) {
        ClientSession session = mongoService.getStartedSession();
        try {
            session.startTransaction();
            // Goal
            Document goalDoc = mongoService.updateDocById(session, mid, GOAL_COLLECTION_NAME, goal);
            if (goalDoc == null) throw new NotFoundException("");
            // Goal Stats in Goal
            if (goalStatsUpdate != null && !goalStatsUpdate.isEmpty()) {
                goalDoc = mongoService.updateSetEmbeddedDocByParentId(session, mid, GOAL_COLLECTION_NAME, STATS_NAME, goalStatsUpdate);
                if (goalDoc == null) throw new NotFoundException("");
            }
            // Partial Goal in User
            Document userDoc;
            if (partialGoalUpdate != null && !partialGoalUpdate.isEmpty()) {
                userDoc = mongoService.updateEmbeddedDocInListByParentKeySonId(session, USER_ID_NAME, uid,
                        USER_COLLECTION_NAME, GOAL_LIST_NAME, mid, partialGoalUpdate);
                if (userDoc == null) throw new NotFoundException("");
            }
            // User Stats in User
            if (userStatsUpdate != null && !userStatsUpdate.isEmpty()) {
                userDoc = mongoService.updateIncEmbeddedDocByParentKey(session, USER_ID_NAME, uid, USER_COLLECTION_NAME,
                        STATS_NAME, userStatsUpdate);
                if (userDoc == null) throw new Exception();
                Document composedDoc = new Document();
                composedDoc.append(STATS_NAME, userDoc.get(STATS_NAME, Document.class));
                composedDoc.append("meta", goalDoc);
                session.commitTransaction();
                session.close();
                return composedDoc;
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
            DeleteResult deleteGoal = mongoService.deleteDocById(session, mid, GOAL_COLLECTION_NAME);
            if (!deleteGoal.wasAcknowledged()) throw new Exception();
            if (deleteGoal.getDeletedCount() == 0) throw new NotFoundException("");
            Document userDoc = mongoService.removeEmbeddedDocInListByParentKeySonId(session, USER_ID_NAME, uid, USER_COLLECTION_NAME,
                    GOAL_LIST_NAME, mid);
            if (userDoc == null) throw new NotFoundException("");
            userDoc = mongoService.updateIncEmbeddedDocByParentKey(session, USER_ID_NAME, uid, USER_COLLECTION_NAME,
                    STATS_NAME, userStatsUpdate);
            if (userDoc == null) throw new Exception();
            session.commitTransaction();
            session.close();
            return userDoc.get(STATS_NAME, Document.class);
        } catch (NotFoundException e) {
            session.abortTransaction();
            session.close();
            throw new NotFoundException("Meta no encontrada");
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al borrar la meta", e);
        }
    }
}