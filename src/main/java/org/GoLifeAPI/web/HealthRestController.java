package org.GoLifeAPI.web;

import org.GoLifeAPI.infrastructure.FirebaseService;
import org.GoLifeAPI.infrastructure.KeyManagementService;
import org.GoLifeAPI.infrastructure.MongoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.KeyManager;

@RestController
@RequestMapping("/api/salud")
public class HealthRestController {

    private final MongoService mongoService;
    private final FirebaseService firebaseService;
    private final KeyManagementService keyManagementService;

    public HealthRestController(MongoService mongoService,
                                FirebaseService firebaseService,
                                KeyManagementService keyManagementService) {
        this.mongoService = mongoService;
        this.firebaseService = firebaseService;
        this.keyManagementService = keyManagementService;
    }

    @GetMapping()
    public ResponseEntity<?> healthCheck() {
        try {
            boolean mongoOk = mongoService.ping();
            boolean firebaseOk = firebaseService.isAvailable();
            boolean keyManagementOk = keyManagementService.ping();
            if (mongoOk && firebaseOk && keyManagementOk) {
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