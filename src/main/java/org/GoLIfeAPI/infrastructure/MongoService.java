package org.GoLIfeAPI.infrastructure;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MongoService {

    private static final String CONNECTION_STRING = System.getenv("DB_CONNECTION_STRING");
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

    public MongoClient getClient() {
        return mongoClient;
    }

    public ClientSession getStartedSession() {
        return mongoClient.startSession();
    }
}