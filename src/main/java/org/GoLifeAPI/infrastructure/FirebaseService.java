package org.GoLifeAPI.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FirebaseService {

    @PostConstruct
    private void initializeFirebaseService() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(System.getenv("FIREBASE_PROJECT_ID"))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            System.err.println("Error al inicializar Firebase: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar Firebase", e);
        }
    }

    public boolean isAvailable() {
        try {
            FirebaseAuth.getInstance().getUser("invalidUID");
            return true;
        } catch (FirebaseAuthException e) {
            return e.getAuthErrorCode() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String verifyBearerToken(String token) {
        try {
            token = token.substring(7); // erases "Bearer "
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            return decodedToken.getUid();
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