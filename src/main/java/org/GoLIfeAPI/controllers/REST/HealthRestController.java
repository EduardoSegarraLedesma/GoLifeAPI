package org.GoLIfeAPI.controllers.REST;

import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.MongoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthRestController {

    private final MongoService mongoService;
    private final FirebaseService firebaseService;

    public HealthRestController(MongoService mongoService, FirebaseService firebaseService) {
        this.mongoService = mongoService;
        this.firebaseService = firebaseService;
    }

    @GetMapping()
    public ResponseEntity<?> healthCheck() {
        try {
            boolean mongoOk = mongoService.ping();
            boolean firebaseOk = firebaseService.isAvailable();
            if (mongoOk && firebaseOk) {
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Algunos servicios no estan disponibles");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Comprobacion de salud fallida: Error Interno");
        }
    }
}