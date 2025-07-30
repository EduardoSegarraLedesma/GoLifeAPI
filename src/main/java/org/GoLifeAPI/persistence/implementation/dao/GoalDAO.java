package org.GoLifeAPI.persistence.implementation.dao;

import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.GoLifeAPI.infrastructure.MongoService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GoalDAO extends BaseDAO {

    private static final String RECORD_LIST_NAME = "registros";

    @Autowired
    public GoalDAO(MongoService mongoService) {
        super(mongoService, "Goals");
    }

    // Insert ->
    public Document insertRecordInListByGoalId(ClientSession session,
                                               String id, Document doc) {
        Document order = new Document("$each", List.of(doc))
                .append("$sort", new Document("fecha", -1));
        return insertEmbeddedDocInListByParentKey(session,
                GOAL_ID_NAME, new ObjectId(id), RECORD_LIST_NAME, order);
    }

    // Update
    public Document updateGoalByGoalId(ClientSession session,
                                       String id, Document update) {
        return updateDocByKey(session, GOAL_ID_NAME, new ObjectId(id), update);
    }

    public Document updateGoalSatsByGoalId(ClientSession session,
                                           String id, Document update) {
        List<Bson> updates = prepareSetInEmbeddedDoc(STATS_NAME, update);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        new Document(GOAL_ID_NAME, new ObjectId(id)),
                        Updates.combine(updates),
                        opts);
    }

    // Delete
    public DeleteResult deleteGoalByGoalId(ClientSession session,
                                           String goalId) {
        return deleteDocByKey(session, GOAL_ID_NAME, new ObjectId(goalId));
    }

    public DeleteResult deleteManyGoalsByUid(ClientSession session,
                                             String uid) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .deleteMany(session, new Document(USER_ID_NAME, uid));
    }

    public Document removeRecordFromListByGoalIdAndDate(ClientSession session,
                                                        String goalId, String dateValue) {
        return removeEmbeddedDocInListByParentKeySonKey(session,
                GOAL_ID_NAME, new ObjectId(goalId), RECORD_LIST_NAME, "fecha", dateValue);
    }

    // Update Doc Preparation Methods
    private List<Bson> prepareSetInEmbeddedDoc(String embeddedKey, Document update) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : update.keySet()) {
            updates.add(Updates.set(embeddedKey + "." + fieldName, update.get(fieldName)));
        }
        return updates;
    }
}