package org.GoLIfeAPI.controllers.REST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.Models.DTOs.*;
import org.GoLIfeAPI.Models.Goals.*;
import org.GoLIfeAPI.controllers.Persistence.GoalPersistenceController;
import org.GoLIfeAPI.services.FirebaseService;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metas")
public class GoalRestController extends BaseRestController {

    private final GoalPersistenceController goalPersistenceController;

    public GoalRestController(FirebaseService firebaseService,
                              GoalPersistenceController goalPersistenceController) {
        super(firebaseService);
        this.goalPersistenceController = goalPersistenceController;
    }

    @PostMapping("/bool")
    public ResponseEntity<?> postMetaBool(@RequestHeader("Authorization") String authorizationHeader,
                                          @RequestBody BoolGoal goal) {
        return postMeta(authorizationHeader, goal);
    }

    @PostMapping("/num")
    public ResponseEntity<?> postMetaNum(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody NumGoal goal) {
        return postMeta(authorizationHeader, goal);
    }

    @GetMapping("/{mid}")
    public ResponseEntity<?> getMeta(@RequestHeader("Authorization") String authorizationHeader,
                                     @PathVariable("mid") String mid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;

        Document doc = goalPersistenceController.read(mid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado la meta");
    }

    @PostMapping("/{mid}/finalizar")
    public ResponseEntity<?> postMetaFinalizar(@RequestHeader("Authorization") String authorizationHeader,
                                               @PathVariable("mid") String mid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        UpdateBoolGoalDTO update = new UpdateBoolGoalDTO();
        update.setFinalizado(true);
        return patchMetaAux(uid, mid, update);
    }

    @PatchMapping("/{mid}")
    public ResponseEntity<?> patchMeta(@RequestHeader("Authorization") String authorizationHeader,
                                       @PathVariable("mid") String mid,
                                       @RequestBody String jsonBody) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document meta = goalPersistenceController.read(mid);
        if (meta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Meta no encontrada.");
        }
        String tipo = meta.getString("tipo");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if ("Bool".equalsIgnoreCase(tipo)) {
                UpdateBoolGoalDTO updateDTO = objectMapper.readValue(jsonBody, UpdateBoolGoalDTO.class);
                return patchMetaAux(uid, mid, updateDTO);
            } else if ("Num".equalsIgnoreCase(tipo)) {
                UpdateNumGoalDTO updateDTO = objectMapper.readValue(jsonBody, UpdateNumGoalDTO.class);
                return patchMetaAux(uid, mid, updateDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de meta no soportado: " + tipo);
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON malformado o inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    @DeleteMapping("/{mid}")
    public ResponseEntity<?> deleteMeta(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable("mid") String mid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        if (goalPersistenceController.delete(mid, uid)) return ResponseEntity.ok("Meta eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar la Meta");
    }

    // Funciones Auxiliares

    private ResponseEntity<?> postMeta(String authorizationHeader, Goal goal) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document doc = goalPersistenceController.create(goal, uid);
        if (doc != null) return ResponseEntity.status(HttpStatus.CREATED).body(doc.toJson());
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear la meta");
    }

    private ResponseEntity<?> patchMetaAux(String authorizationHeader, String mid, UpdateGoalDTO update) {
        Document updated = goalPersistenceController.update(update, authorizationHeader, mid);
        if (updated != null) {
            return ResponseEntity.ok(updated.toJson());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido actualizar la meta");
        }
    }
}