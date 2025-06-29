package org.GoLIfeAPI.controllers.REST;

import jakarta.validation.Valid;
import org.GoLIfeAPI.controllers.Persistence.UserPersistenceController;
import org.GoLIfeAPI.models.DTOs.CreateUserDTO;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UserRestController {

    private final UserPersistenceController userPersistenceController;

    public UserRestController(
            UserPersistenceController userPersistenceController) {
        this.userPersistenceController = userPersistenceController;
    }

    @PostMapping
    public ResponseEntity<?> postUser(@AuthenticationPrincipal String uid,
                                      @Valid @RequestBody CreateUserDTO userDTO) {
        Document doc = userPersistenceController.read(uid);
        if (doc != null) return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya existe");
        doc = userPersistenceController.create(userDTO.getUserPOJO(uid), uid);
        if (doc != null) return ResponseEntity.status(HttpStatus.CREATED).body(doc.toJson());
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear al usuario");
    }

    @GetMapping
    public ResponseEntity<?> getUser(@AuthenticationPrincipal String uid) {
        Document doc = userPersistenceController.read(uid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado al usuario");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal String uid) {
        if (userPersistenceController.delete(uid)) return ResponseEntity.ok("Usuario eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar al usuario");
    }
}