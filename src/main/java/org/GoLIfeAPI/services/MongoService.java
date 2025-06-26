package org.GoLIfeAPI.services;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MongoService {

    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private MongoClient mongoClient;

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

    // Funciones para Querys

    public String insertOne(ClientSession session, Document doc, String collection) {
        InsertOneResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .insertOne(session, doc);
        return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue().toString();
    }

    public Boolean insertOneEmbeddedDocByParentId(ClientSession session, String id, String collection,
                                                  String listKey, Document doc) {
        return insertOneEmbeddedDocByParentKey(session, "_id", new ObjectId(id), collection, listKey, doc);
    }

    public Boolean insertOneEmbeddedDocByParentKey(ClientSession session, String key, Object value,
                                                   String collection, String listKey, Document doc) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        new Document(key, value),
                        new Document("$push", new Document(listKey, doc)));
        return result.getModifiedCount() == 1;
    }

    public Document findOneById(String id, String collection) {
        return findOneByKey("_id", new ObjectId(id), collection);
    }

    public Document findOneByKey(String key, Object value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .find(new Document(key, value)).first();
    }

    public Boolean updateOneById(ClientSession session, String id, String collection, Document update) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        new Document("_id", new ObjectId(id)),
                        new Document("$set", update));
        return result.getModifiedCount() == 1;
    }

    public Boolean updateOneEmbeddedDocByParentKeySonId(ClientSession session, String key, String value,
                                                        String collection, String listKey, String docId,
                                                        Document doc) {
        List<Bson> updates = new ArrayList<>();
        for (String fieldName : doc.keySet()) {
            updates.add(Updates.set(listKey + ".$." + fieldName, doc.get(fieldName)));
        }
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        Filters.and(
                                Filters.eq(key, value),
                                Filters.eq(listKey + "._id", new ObjectId(docId))),
                        Updates.combine(updates));
        return result.getModifiedCount() == 1;
    }

    public Boolean deleteOneById(ClientSession session, String id, String collection) {
        return deleteOneByKey(session, "_id", new ObjectId(id), collection);
    }

    public Boolean deleteOneByKey(ClientSession session, String key, Object value, String collection) {
        DeleteResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteOne(session, new Document(key, value));
        return result.getDeletedCount() == 1;
    }

    public Boolean deleteManyByKey(ClientSession session, String key, String value, String collection) {
        DeleteResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteMany(session, new Document(key, value));
        return result.getDeletedCount() > 0;
    }


    public Boolean deleteOneEmbeddedDocByParentKeySonId(ClientSession session, String key, Object value,
                                                        String collection, String listKey, String listId) {
        return deleteOneEmbeddedDocByParentKeySonKey(session, key, value, collection, listKey, "_id", new ObjectId(listId));
    }

    public Boolean deleteOneEmbeddedDocByParentIdSonKey(ClientSession session, String id,
                                                        String collection, String listKey, String listField, String listValue) {
        return deleteOneEmbeddedDocByParentKeySonKey(session, "_id", new ObjectId(id), collection, listKey, listField, listValue);
    }

    public Boolean deleteOneEmbeddedDocByParentKeySonKey(ClientSession session, String key, Object value,
                                                         String collection, String listKey, String fieldKey, Object fieldValue) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        Filters.eq(key, value),
                        Updates.pull(listKey, Filters.eq(fieldKey, fieldValue)));
        return result.getModifiedCount() == 1;
    }
}