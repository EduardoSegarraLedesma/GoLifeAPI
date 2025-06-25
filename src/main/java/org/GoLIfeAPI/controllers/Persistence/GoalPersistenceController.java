package org.GoLIfeAPI.controllers.Persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.Models.DTOs.UpdateGoalDTO;
import org.GoLIfeAPI.Models.Goals.Goal;
import org.GoLIfeAPI.services.MongoService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoalPersistenceController extends BasePersistenceController {

    @Autowired
    public GoalPersistenceController(MongoService mongoService) {
        super(mongoService);
    }

    public Document create(Goal goal, String uid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            goal.setUid(uid);
            String objectId = mongoService.insertOne(session, goal.toDocument(), GOAL_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                goal.set_id(new ObjectId(objectId));
                if (mongoService.insertOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                        GOAL_LIST_NAME, goal.toParcialDocument())) {
                    Document doc = mongoService.findOneById(objectId, GOAL_COLLECTION_NAME);
                    session.commitTransaction();
                    session.close();
                    return doc;
                } else throw new Exception();
            } else throw new Exception();
        } catch (
                Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Document read(String id) {
        return mongoService.findOneById(id, GOAL_COLLECTION_NAME);
    }

    public Document update(UpdateGoalDTO update, String uid, String mid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (!mongoService.updateOneById(session, mid, GOAL_COLLECTION_NAME, update.toDocument())) {
                Document parcialDoc = update.toParcialDocument();
                if (!parcialDoc.isEmpty()) {
                    if (mongoService.updateOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                            GOAL_LIST_NAME, mid, update.toParcialDocument())) {
                        session.commitTransaction();
                        session.close();
                        return mongoService.findOneById(mid, GOAL_COLLECTION_NAME);
                    } else throw new Exception();
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Boolean delete(String mid, String uid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (mongoService.deleteOneById(session, mid, GOAL_COLLECTION_NAME)) {
                if (mongoService.deleteOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                        GOAL_LIST_NAME, mid)) {
                    session.commitTransaction();
                    session.close();
                    return true;
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return false;
        }
    }
}
