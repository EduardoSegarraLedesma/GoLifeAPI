package org.GoLIfeAPI.services;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import com.mongodb.client.result.InsertOneResult;
import org.GoLIfeAPI.Models.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PersistenceService {

    private final FirebaseService firebaseService;
    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private MongoClient mongoClient;
    private ClientSession session;

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
        session = mongoClient.startSession();
        try {
            session.startTransaction();
            user.setId(uid);
            Document doc = insertOne(user.toDocument(), "Users");
            session.commitTransaction();
            return doc;
        } catch (Exception e) {
            session.abortTransaction();
            firebaseService.deleteFirebaseUser(uid);
            return null;
        }
    }

    public Document readUser(String id) {
        return findOneByKey("id", id, "Users");
    }

    public Boolean deleteUser(String id) {
        session = mongoClient.startSession();
        try {
            session.startTransaction();
            deleteOneByKey("id", id, "Users");
            deleteManyByKey("uid", id, "Goals");
            if (firebaseService.deleteFirebaseUser(id)) {
                session.commitTransaction();
                return true;
            } else {
                session.abortTransaction();
                return false;
            }
        } catch (Exception e) {
            session.abortTransaction();
            return false;
        }
    }

//Funciones Auxiliares para Querys

    private Document insertOne(Document doc, String collection) {
        InsertOneResult result = mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .insertOne(doc);
        return findOneById(result.getInsertedId().asObjectId().getValue().toString(), collection);
    }

    private Document findOneById(String id, String collection) {
        ObjectId objectId = new ObjectId(id);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .find(new Document("_id", objectId)).first();
    }

    private Document findOneByKey(String key, String value, String collection) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .find(new Document(key, value)).first();
    }

    private void deleteOneById(String id, String collection) {
        ObjectId objectId = new ObjectId(id);
        mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteOne(new Document("_id", objectId));
    }

    private void deleteOneByKey(String key, String value, String collection) {
        mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteOne(new Document(key, value));
    }

    private void deleteManyByKey(String key, String value, String collection) {
        mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(collection)
                .deleteMany(new Document(key, value));
    }
}