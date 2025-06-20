package org.GoLIfeAPI.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseService {

    @PostConstruct
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

    public boolean deleteFirebaseUser(String uid) {
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            return true;
        } catch (FirebaseAuthException e) {
            return false;
        }
    }


}