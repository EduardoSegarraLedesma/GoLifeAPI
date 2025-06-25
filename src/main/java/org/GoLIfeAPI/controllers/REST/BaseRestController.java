package org.GoLIfeAPI.controllers.REST;

import org.GoLIfeAPI.services.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseRestController {

    private final FirebaseService firebaseService;

    public BaseRestController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
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