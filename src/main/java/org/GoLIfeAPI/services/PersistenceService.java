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
import org.GoLIfeAPI.Models.DTOs.UpdateGoalDTO;
import org.GoLIfeAPI.Models.Goals.Goal;
import org.GoLIfeAPI.Models.Records.Record;
import org.GoLIfeAPI.Models.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersistenceService {

    private final FirebaseService firebaseService;
    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private static final String USER_COLLECTION_NAME = "Users";
    private static final String GOAL_COLLECTION_NAME = "Goals";
    private MongoClient mongoClient;

    public PersistenceService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostConstruct
    private void initializePersistenceService() {
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

    public Document createUser(User user, String uid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            user.setId(uid);
            String objectId = insertOne(session, user.toDocument(), USER_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                Document doc = findOneById(objectId, USER_COLLECTION_NAME);
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

    public Document readUser(String id) {
        return findOneByKey("id", id, USER_COLLECTION_NAME);
    }

    public Boolean deleteUser(String id) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            if (deleteOneByKey(session, "id", id, USER_COLLECTION_NAME)) {
                if (deleteManyByKey(session, "uid", id, GOAL_COLLECTION_NAME)) {
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

    public Document createGoal(Goal goal, String uid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            goal.setUid(uid);
            String objectId = insertOne(session, goal.toDocument(), GOAL_COLLECTION_NAME);
            if (objectId != null && !objectId.isBlank()) {
                goal.set_id(new ObjectId(objectId));
                if (insertOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                        "metas", goal.toParcialDocument())) {
                    Document doc = findOneById(objectId, GOAL_COLLECTION_NAME);
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

    public Document readGoal(String id) {
        return findOneById(id, GOAL_COLLECTION_NAME);
    }

    public Document updateGoal(UpdateGoalDTO update, String uid, String mid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            if (!updateOneById(session, mid, GOAL_COLLECTION_NAME, update.toDocument())) {
                Document parcialDoc = update.toParcialDocument();
                if (!parcialDoc.isEmpty()) {
                    if (updateOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                            "metas", mid, update.toParcialDocument())) {
                        session.commitTransaction();
                        session.close();
                        return findOneById(mid, GOAL_COLLECTION_NAME);
                    } else throw new Exception();
                } else throw new Exception();
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Boolean deleteGoal(String mid, String uid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            if (deleteOneById(session, mid, GOAL_COLLECTION_NAME)) {
                if (deleteOneEmbeddedDocByFieldKey(session, "id", uid, USER_COLLECTION_NAME,
                        "metas", mid)) {
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

    public Document createRecord(Record record, String mid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            if (insertOneEmbeddedDocByParentId(session, mid, GOAL_COLLECTION_NAME,
                    "registros", record.toDocument())) {
                Document doc = findOneById(mid, GOAL_COLLECTION_NAME);
                session.commitTransaction();
                session.close();
                return doc;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return null;
        }
    }

    public Boolean deleteRecord(String mid, String rid) {
        ClientSession session = mongoClient.startSession();
        try {
            session.startTransaction();
            if (deleteOneEmbeddedDocByParentId(session, mid, USER_COLLECTION_NAME,
                    "registros", rid)) {
                session.commitTransaction();
                session.close();
                return true;
            } else throw new Exception();
        } catch (Exception e) {
            session.abortTransaction();
            session.close();
            return false;
        }
    }


// Funciones Auxiliares para Querys

    private String insertOne(ClientSession session, Document doc, String collection) {
        InsertOneResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .insertOne(session, doc);
        return result.getInsertedId().asObjectId().getValue().toString();
    }

    private Boolean insertOneEmbeddedDocByParentId(ClientSession session, String id, String collection,
                                                   String listKey, Document doc) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        new Document("_id", new ObjectId(id)),
                        new Document("$push", new Document(listKey, doc)));
        return result.getModifiedCount() == 1;
    }

    private Boolean insertOneEmbeddedDocByFieldKey(ClientSession session, String key, String value,
                                                   String collection, String listKey, Document doc) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        new Document(key, value),
                        new Document("$push", new Document(listKey, doc)));
        return result.getModifiedCount() == 1;
    }

    private Document findOneById(String id, String collection) {
        return findOneByKey("_id", new ObjectId(id), collection);
    }

    private Document findOneByKey(String key, Object value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .find(new Document(key, value)).first();
    }

    private Boolean updateOneById(ClientSession session, String id, String collection, Document update) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        new Document("_id", new ObjectId(id)),
                        new Document("$set", update));
        return result.getModifiedCount() == 1;
    }

    private Boolean updateOneEmbeddedDocByFieldKey(ClientSession session, String key, String value,
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

    private Boolean deleteOneById(ClientSession session, String id, String collection) {
        return deleteOneByKey(session, "_id", new ObjectId(id), collection);
    }

    private Boolean deleteOneByKey(ClientSession session, String key, Object value, String collection) {
        DeleteResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteOne(session, new Document(key, value));
        return result.getDeletedCount() == 1;
    }

    private Boolean deleteManyByKey(ClientSession session, String key, String value, String collection) {
        DeleteResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteMany(session, new Document(key, value));
        return result.getDeletedCount() > 0;
    }

    private Boolean deleteOneEmbeddedDocByParentId(ClientSession session, String id, String collection,
                                                   String listKey, String docId) {
        return deleteOneEmbeddedDocByFieldKey(session, "_id", new ObjectId(id), collection, listKey, docId);
    }

    private Boolean deleteOneEmbeddedDocByFieldKey(ClientSession session, String key, Object value,
                                                   String collection, String listKey, String docId) {
        UpdateResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .updateOne(session,
                        Filters.eq(key, value),
                        Updates.pull(listKey, Filters.eq("_id", new ObjectId(docId))));
        return result.getModifiedCount() == 1;
    }
}