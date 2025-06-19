package org.GoLIfeAPI.controllers;

import org.GoLIfeAPI.Models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.bson.Document;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        String uid = FirebaseController.getInstance().verifyBearerToken(authorizationHeader);
        if (uid == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Acceso restringido: token inválido o ausente");
        }
        Document doc = PersistenceController.getInstance().getUser(uid);
        return ResponseEntity.ok(doc.toJson());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody User user) {
        String uid = FirebaseController.getInstance().verifyBearerToken(authorizationHeader);
        if (uid == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Acceso restringido: token inválido o ausente");
        }
        Document doc = PersistenceController.getInstance().createUser(user, uid);
        return ResponseEntity.ok(doc.toJson());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authorizationHeader) {
        String uid = FirebaseController.getInstance().verifyBearerToken(authorizationHeader);
        if (uid == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Acceso restringido: token inválido o ausente");
        }
        PersistenceController.getInstance().deleteUser(uid);
        return ResponseEntity.ok("Usuario eliminado exitosamente");
    }

}