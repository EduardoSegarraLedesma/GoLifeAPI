package org.GoLIfeAPI.infrastructure;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class MongoService {

    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private MongoClient mongoClient;
    private FindOneAndUpdateOptions opts;

    @PostConstruct
    private void initializeMongoService() {
        try {
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .serverApi(serverApi)
                    .build();
            mongoClient = MongoClients.create(settings);
            opts = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER);
        } catch (MongoSecurityException e) {
            System.err.println("Autenticación fallida con MongoDB.");
            throw new RuntimeException("Autenticación fallida con BD: " + e.getMessage(), e);
        } catch (MongoTimeoutException e) {
            System.err.println("Tiempo de espera agotado al conectar con MongoDB.");
            throw new RuntimeException("Tiempo de espera agotado al conectar con la BD: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar conexion con la BD: " + e.getMessage(), e);
        }
    }

    public boolean ping() {
        try {
            Document result = mongoClient.getDatabase("admin")
                    .runCommand(new Document("ping", 1));
            Object okValue = result.get("ok");
            if (okValue instanceof Number) {
                return ((Number) okValue).doubleValue() == 1.0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public ClientSession getSession() {
        return mongoClient.startSession();
    }

    // Low Level Query Methods:

    // Insert new Document in Collection ->
    public String insertDoc(ClientSession session, Document doc, String collection) {
        InsertOneResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .insertOne(session, doc);
        return result.getInsertedId().asObjectId().getValue().toString();
    }

    // Insert new Document in Embedded Document List ->
    public Document insertEmbeddedDocInListByParentId(ClientSession session, String id,
                                                      String collection, String listKey, Document doc) {
        return insertEmbeddedDocInListByParentKey(session, "_id", new ObjectId(id), collection, listKey, doc);
    }

    public Document insertEmbeddedDocInListByParentKey(ClientSession session, String key, Object value,
                                                       String collection, String listKey, Document doc) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        new Document("$push", new Document(listKey, doc)),
                        opts);
    }

    // Get Document ->
    public Document findDocById(String id, String collection) {
        return findDocByKey("_id", new ObjectId(id), collection);
    }

    public Document findDocByKey(String key, Object value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .find(new Document(key, value)).first();
    }

    // Update Document and Get Document ->
    public Document updateDocById(ClientSession session, String id,
                                  String collection, Document update) {
        return updateDocByKey(session, "_id", new ObjectId(id), collection, update);
    }

    public Document updateDocByKey(ClientSession session, String key, Object value,
                                   String collection, Document update) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        new Document("$set", update),
                        opts);
    }

    // Update Embedded Document and get Full Parent Document ->
    public Document updateSetEmbeddedDocByParentId(ClientSession session, String id,
                                                   String collection, String embeddedKey, Document update) {
        return updateSetEmbeddedDocByParentKey(session, "_id", new ObjectId(id), collection, embeddedKey, update);
    }

    public Document updateSetEmbeddedDocByParentKey(ClientSession session, String key, Object value,
                                                    String collection, String embeddedKey, Document update) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        new Document("$set",
                                new Document(embeddedKey, update)),
                        opts);
    }

    public Document updateIncEmbeddedDocByParentId(ClientSession session, String id,
                                                   String collection, String embeddedKey, Document update) {

        return updateIncEmbeddedDocByParentKey(session, "_id", new ObjectId(id), collection, embeddedKey, update);
    }

    public Document updateIncEmbeddedDocByParentKey(ClientSession session, String key, Object value,
                                                    String collection, String embeddedKey, Document update) {

        List<Bson> updates = prepareIncInEmbeddedDoc(embeddedKey, update);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        new Document(key, value),
                        Updates.combine(updates),
                        opts);
    }

    // Update Embedded Document in Document List and get Full Parent Document ->
    public Document updateEmbeddedDocInListByParentKeySonId(ClientSession session, String key, String value,
                                                            String collection, String listKey, String docId,
                                                            Document update) {
        List<Bson> updates = prepareSetInEmbeddedDocList(listKey, update);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        Filters.and(
                                Filters.eq(key, value),
                                Filters.eq(listKey + "._id", new ObjectId(docId))),
                        Updates.combine(updates),
                        opts);
    }

    // Delete Embedded Document in Document List and get Full Parent Document ->
    public Document removeEmbeddedDocInListByParentKeySonId(ClientSession session, String key, Object value,
                                                            String collection, String listKey, String listId) {
        return removeEmbeddedDocInListByParentKeySonKey(session, key, value, collection, listKey, "_id", new ObjectId(listId));
    }

    public Document removeEmbeddedDocInListByParentIdSonKey(ClientSession session, String id,
                                                            String collection, String listKey,
                                                            String listField, String listValue) {
        return removeEmbeddedDocInListByParentKeySonKey(session, "_id", new ObjectId(id), collection, listKey, listField, listValue);
    }

    public Document removeEmbeddedDocInListByParentKeySonKey(ClientSession session,
                                                             String key, Object value,
                                                             String collection, String listKey,
                                                             String fieldKey, Object fieldValue) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .findOneAndUpdate(session,
                        Filters.eq(key, value),
                        Updates.pull(listKey, Filters.eq(fieldKey, fieldValue)),
                        opts);
    }

    // Delete Document/s ->
    public DeleteResult deleteDocById(ClientSession session, String id, String collection) {
        return deleteDocByKey(session, "_id", new ObjectId(id), collection);
    }

    public DeleteResult deleteDocByKey(ClientSession session, String key, Object value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteOne(session, new Document(key, value));
    }

    public DeleteResult deleteManyDocsByKey(ClientSession session, String key, String value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteMany(session, new Document(key, value));
    }

    // Operation Preparation Methods:

    public List<Bson> prepareSetInEmbeddedDocList(String listKey, Document update) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : update.keySet()) {
            updates.add(Updates.set(listKey + ".$." + fieldName, update.get(fieldName)));
        }
        return updates;
    }

    public List<Bson> prepareIncInEmbeddedDoc(String embeddedKey, Document update) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : update.keySet()) {
            updates.add(Updates.inc(embeddedKey + "." + fieldName, update.getInteger(fieldName)));
        }
        return updates;
    }
}