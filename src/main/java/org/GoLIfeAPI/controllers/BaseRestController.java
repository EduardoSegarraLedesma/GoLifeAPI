package org.GoLIfeAPI.controllers;

import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseRestController {

    private final FirebaseService firebaseService;
    protected final PersistenceService persistenceService;

    public BaseRestController(FirebaseService firebaseService, PersistenceService persistenceService) {
        this.firebaseService = firebaseService;
        this.persistenceService = persistenceService;
    }

    protected ResponseEntity<?> validateToken(String token) {
        String uid = firebaseService.verifyBearerToken(token);
        if (uid == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Acceso restringido");
        }
        return ResponseEntity.ok(uid);
    }
}