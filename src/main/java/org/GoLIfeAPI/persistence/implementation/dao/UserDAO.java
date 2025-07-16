package org.GoLIfeAPI.persistence.implementation.dao;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDAO extends BaseDAO {

    private static final String GOAL_LIST_NAME = "metas";

    @Autowired
    public UserDAO(MongoService mongoService) {
        super(mongoService, "Users");
    }

    // Insert ->
    public Document insertPartialGoalInListByUid(ClientSession session,
                                                 String uid, Document doc) {
        return insertEmbeddedDocInListByParentKey(session,
                USER_ID_NAME, uid, GOAL_LIST_NAME, doc);
    }

    // Find ->
    public Document findUserByUid(Object uid) {
        return findDocByKey(USER_ID_NAME, uid);

    }

    // Update
    public Document updateUserByUid(ClientSession session,
                                    String uid, Document update) {
        return updateDocByKey(session, USER_ID_NAME, uid, update);
    }

    public Document updatePartialGoalInListByUidAndGoalId(ClientSession session,
                                                          String uid, String goalId, Document update) {
        List<Bson> updates = prepareSetInEmbeddedDocList(GOAL_LIST_NAME, update);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        Filters.and(
                                Filters.eq(USER_ID_NAME, uid),
                                Filters.eq(GOAL_LIST_NAME + "." + GOAL_ID_NAME,
                                        new ObjectId(goalId))),
                        Updates.combine(updates),
                        opts);
    }

    public Document updateUserStatsByUid(ClientSession session,
                                         Object uid, Document update) {
        List<Bson> updates = prepareIncInEmbeddedDoc(STATS_NAME, update);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        new Document(USER_ID_NAME, uid),
                        Updates.combine(updates),
                        opts);
    }

    // Delete ->
    public DeleteResult deleteUserByUid(ClientSession session,
                                        String uid) {
        return deleteDocByKey(session, USER_ID_NAME, uid);
    }

    public Document removePartialGoalFromListByUidAndGoalId(ClientSession session,
                                                            Object uid, String goalId) {
        return removeEmbeddedDocInListByParentKeySonKey(session,
                USER_ID_NAME, uid, GOAL_LIST_NAME, GOAL_ID_NAME, new ObjectId(goalId));
    }

    // Update Doc Preparation Methods
    private List<Bson> prepareSetInEmbeddedDocList(String listKey, Document update) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : update.keySet()) {
            updates.add(Updates.set(listKey + ".$." + fieldName, update.get(fieldName)));
        }
        return updates;
    }

    private List<Bson> prepareIncInEmbeddedDoc(String embeddedKey, Document update) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : update.keySet()) {
            updates.add(Updates.inc(embeddedKey + "." + fieldName, update.getInteger(fieldName)));
        }
        return updates;
    }
}