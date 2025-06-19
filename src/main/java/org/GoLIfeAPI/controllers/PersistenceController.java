package org.GoLIfeAPI.controllers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.GoLIfeAPI.Models.User;
import org.bson.Document;
import org.bson.types.ObjectId;

public class PersistenceController {

    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
    private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private MongoClient mongoClient;

    private static PersistenceController instance = null;

    public static PersistenceController getInstance() {
        if (instance == null) {
            instance = new PersistenceController();
        }
        return instance;
    }

    private PersistenceController() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                .serverApi(serverApi)
                .build();
        mongoClient = MongoClients.create(settings);
    }

    public Document createUser(User user, String uid) {
        user.setId(uid);
        mongoClient.getDatabase(DATABASE_NAME)
                .getCollection("Users")
                .insertOne(user.toDocument());
        return getUser(uid);
    }

    public Document getUser(String id) {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection("Users")
                .find(new Document("id", id)).first();
    }

    public void deleteUser(String id) {
        mongoClient.getDatabase(DATABASE_NAME)
                .getCollection("Users")
                .deleteOne(new Document("id", id));
    }

    public Document getGoal(String id) {
        ObjectId objectId = new ObjectId(id);
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection("Goals")
                .find(new Document("_id", objectId)).first();
    }

    // Cierra el cliente Mongo
    private void close() {
        synchronized (PersistenceController.class) {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
            }
        }
    }
}