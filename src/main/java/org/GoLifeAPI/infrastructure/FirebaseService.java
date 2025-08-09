package org.GoLifeAPI.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    @PostConstruct
    private void initializeFirebaseService() {
        final long t0 = System.currentTimeMillis();
        final String fbProject = System.getenv("FIREBASE_PROJECT_ID");
        boolean ok = false;

        try {
            if (fbProject == null || fbProject.isBlank()) {
                throw new IllegalStateException("FIREBASE_PROJECT_ID no está definido");
            }

            GoogleCredentials cred = GoogleCredentials.getApplicationDefault();
            log.info("[FB] init starting | fbProject={}, credsClass={}",
                    fbProject, cred.getClass().getSimpleName());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(cred)
                    .setProjectId(fbProject)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("[FB] initialized OK | optionsProject={}",
                        FirebaseApp.getInstance().getOptions().getProjectId());
            } else {
                log.info("[FB] already initialized | optionsProject={}",
                        FirebaseApp.getInstance().getOptions().getProjectId());
            }

            ok = true;
        } catch (Exception e) {
            log.error("[FB] init FAILED | fbProject={}", fbProject, e);
            throw new IllegalStateException("No se pudo inicializar Firebase", e);
        } finally {
            log.info("[FB] init finished | status={}, elapsedMs={}",
                    ok ? "OK" : "FAILED", System.currentTimeMillis() - t0);
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