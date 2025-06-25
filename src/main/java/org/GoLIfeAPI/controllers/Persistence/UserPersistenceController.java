package org.GoLIfeAPI.controllers.Persistence;

import com.mongodb.client.ClientSession;
import org.GoLIfeAPI.Models.User;
import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.MongoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPersistenceController extends BasePersistenceController {

    private final FirebaseService firebaseService;

    @Autowired
    public UserPersistenceController(MongoService mongoService, FirebaseService firebaseService) {
        super(mongoService);
        this.firebaseService = firebaseService;
    }

    public Document create(User user, String uid) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            user.setId(uid);
            String objectId = mongoService.insertOne(session, user.toDocument(), USER_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                Document doc = mongoService.findOneById(objectId, USER_COLLECTION_NAME);
                session.commitTransaction();
                session.close();
                return doc;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            firebaseService.deleteFirebaseUser(uid);
            return null;
        }
    }

    public Document read(String id) {
        return mongoService.findOneByKey("id", id, USER_COLLECTION_NAME);
    }

    public Boolean delete(String id) {
        ClientSession session = mongoService.getSession();
        try {
            session.startTransaction();
            if (mongoService.deleteOneByKey(session, "id", id, USER_COLLECTION_NAME)) {
                if (mongoService.deleteManyByKey(session, "uid", id, GOAL_COLLECTION_NAME)) {
                    if (firebaseService.deleteFirebaseUser(id)) {
                        session.commitTransaction();
                        session.close();
                        return true;
                    } else throw new Exception();
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return false;
        }
    }
}