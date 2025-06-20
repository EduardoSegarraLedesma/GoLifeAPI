package org.GoLIfeAPI.controllers;

import org.GoLIfeAPI.Models.User;
import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.PersistenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.bson.Document;

@RestController
@RequestMapping("/api/users")
public class UsersRestController extends BaseRestController {

    public UsersRestController(FirebaseService firebaseService, PersistenceService persistenceService) {
        super(firebaseService, persistenceService);
    }

    @PostMapping
    public ResponseEntity<?> postUser(@RequestHeader("Authorization") String authorizationHeader,
                                      @RequestBody User user) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document doc = persistenceService.createUser(user, uid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear al usuario");
    }

    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document doc = persistenceService.readUser(uid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado al usuario");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authorizationHeader) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        if (persistenceService.deleteUser(uid)) return ResponseEntity.ok("Usuario eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar al usuario");
    }
}