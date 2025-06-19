package org.GoLIfeAPI.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseController {

    private static FirebaseController instance = null;

    public static FirebaseController getInstance() {
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }

    private FirebaseController() {
        initializeFirebase();
    }

    public String verifyBearerToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // elimina "Bearer "
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                return decodedToken.getUid();
            } else {
               return null;
            }
        } catch (FirebaseAuthException e) {
            return null;
        }
    }

    private void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase/serviceAccountKey.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            System.err.println("Error al inicializar Firebase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar Firebase", e);
        }
    }
}