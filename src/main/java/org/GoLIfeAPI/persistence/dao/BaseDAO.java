package org.GoLIfeAPI.persistence.dao;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.GoLIfeAPI.infrastructure.MongoService;
import org.bson.Document;
import org.bson.types.ObjectId;

public abstract class BaseDAO {

    protected final MongoClient mongoClient;
    protected static final String DATABASE_NAME = System.getenv("DB_NAME");
    protected final String COLLECTION_NAME;
    protected static final String STATS_NAME = "estadisticas";
    protected static final String USER_ID_NAME = "uid";
    protected static final String GOAL_ID_NAME = "_id";
    protected FindOneAndUpdateOptions opts;

    protected BaseDAO(MongoService mongoService, String collectionName) {
        this.mongoClient = mongoService.getClient();
        this.COLLECTION_NAME = collectionName;
        opts = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);
    }

    // Base/Common Low Level Query Methods:

    // Insert ->
    public String insertDoc(ClientSession session, Document doc) {
        InsertOneResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .insertOne(session, doc);
        return result.getInsertedId().asObjectId().getValue().toString();
    }

    protected Document insertEmbeddedDocInListByParentKey(ClientSession session,
                                                          String key, Object value,
                                                          String listKey, Document doc) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        new Document("$push", new Document(listKey, doc)),
                        opts);
    }

    // Find ->
    public Document findDocById(String id) {
        return findDocByKey(GOAL_ID_NAME, new ObjectId(id));
    }

    public Document findDocById(ClientSession session,
                                String id) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .find(session,
                        new Document(GOAL_ID_NAME, new ObjectId(id))).first();
    }

    protected Document findDocByKey(String key, Object value) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .find(new Document(key, value)).first();
    }

    // Update ->
    protected Document updateDocByKey(ClientSession session,
                                      String key, Object value, Document update) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        new Document("$set", update),
                        opts);
    }

    // Delete ->
    protected Document removeEmbeddedDocInListByParentKeySonKey(ClientSession session,
                                                                String key, Object value,
                                                                String listKey,
                                                                String fieldKey, Object fieldValue) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .findOneAndUpdate(session,
                        Filters.eq(key, value),
                        Updates.pull(listKey, Filters.eq(fieldKey, fieldValue)),
                        opts);
    }

    protected DeleteResult deleteDocByKey(ClientSession session,
                                          String key, Object value) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME)
                .deleteOne(session, new Document(key, value));
    }
}