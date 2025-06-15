package org.GoLIfeAPI.controllers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class PersistenceController {

    //private static final String CONNECTION_STRING = "mongodb+srv://golifepfg:GoLife_PFG_1@prod-golife-euwest-01.by5tqpp.mongodb.net/?retryWrites=true&w=majority&appName=prod-golife-euwest-01";
    //private static final String DATABASE_NAME = "GoLife";
   private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
   private static final String DATABASE_NAME = System.getenv("DB_NAME");
    private MongoClient mongoClient = null;

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

    // Metodo para obtener la base de datos
    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    // Metodo para obtener una colección
    public MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
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
