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

    public Document create(Document goal, Document partialGoal, String uid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            String objectId = mongoService.insertOne(session, goal, GOAL_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                partialGoal.put("_id", new ObjectId(objectId));
                Document userDoc = mongoService.insertOneEmbeddedDocByParentKey(session, "uid", uid, USER_COLLECTION_NAME,
                        GOAL_LIST_NAME, partialGoal);
                if (userDoc == null) throw new Exception();
                session.commitTransaction();
                session.close();
                return mongoService.findOneById(objectId, GOAL_COLLECTION_NAME);
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            throw new RuntimeException("Error interno al crear la meta", e);
        }
    }

    public Document read(String id) {
        try {
            return mongoService.findOneById(id, GOAL_COLLECTION_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Error interno al leer la meta", e);
        }
    }

    public Document update(Document goal, Document partialGoal, String uid, String mid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            Document goalDoc = mongoService.findOneAndUpdateOneById(session, mid, GOAL_COLLECTION_NAME, goal);
            if (goalDoc == null) throw new NotFoundException("");
            if (!partialGoal.isEmpty()) {
                Document userDoc = mongoService.updateOneEmbeddedDocByParentKeySonId(session, "uid", uid,
                        USER_COLLECTION_NAME, GOAL_LIST_NAME, mid, partialGoal);
                if (userDoc == null) throw new NotFoundException("");
                session.commitTransaction();
                session.close();
                return goalDoc;
            } else throw new Exception();
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

    public void delete(String mid, String uid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            DeleteResult deleteGoal = mongoService.deleteOneById(session, mid, GOAL_COLLECTION_NAME);
            if (!deleteGoal.wasAcknowledged()) throw new Exception();
            if (deleteGoal.getDeletedCount() == 0) throw new NotFoundException("");
            Document userDoc = mongoService.deleteOneEmbeddedDocByParentKeySonId(session, "uid", uid, USER_COLLECTION_NAME,
                    GOAL_LIST_NAME, mid);
            if (userDoc == null) throw new NotFoundException("");
            session.commitTransaction();
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