package org.GoLIfeAPI.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.Models.DTOs.*;
import org.GoLIfeAPI.Models.Goals.*;
import org.GoLIfeAPI.Models.Records.*;
import org.GoLIfeAPI.Models.Records.Record;
import org.GoLIfeAPI.services.FirebaseService;
import org.GoLIfeAPI.services.PersistenceService;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metas")
public class GoalsRestController extends BaseRestController {

    public GoalsRestController(FirebaseService firebaseService, PersistenceService persistenceService) {
        super(firebaseService, persistenceService);
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

        Document doc = persistenceService.readGoal(mid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado la meta");
    }

    @PostMapping("/{mid}/finalizar")
    public ResponseEntity<?> postMetaFinalizar(@RequestHeader("Authorization") String authorizationHeader,
                                               @PathVariable("mid") String mid) {
        UpdateBoolGoalDTO update = new UpdateBoolGoalDTO();
        update.setFinalizado(true);
        return patchMeta(authorizationHeader, mid, update);
    }

    @PatchMapping("/bool/{mid}")
    public ResponseEntity<?> patchMetaBool(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody UpdateBoolGoalDTO update,
                                           @PathVariable("mid") String mid) {
        return patchMeta(authorizationHeader, mid, update);
    }

    @PatchMapping("/num/{mid}")
    public ResponseEntity<?> patchMetaNum(@RequestHeader("Authorization") String authorizationHeader,
                                          @RequestBody UpdateNumGoalDTO update,
                                          @PathVariable("mid") String mid) {
        return patchMeta(authorizationHeader, mid, update);
    }

    @DeleteMapping("/{mid}")
    public ResponseEntity<?> deleteMeta(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable("mid") String mid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        if (persistenceService.deleteGoal(mid, uid)) return ResponseEntity.ok("Meta eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar la Meta");
    }

    @PostMapping("/{mid}/registros")
    public ResponseEntity<?> postRegistro(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable("mid") String mid,
                                          @RequestBody String jsonBody) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;

        Document meta = persistenceService.readGoal(mid);
        if (meta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Meta no encontrada.");
        }
        String tipo = meta.getString("tipo");
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            if ("Bool".equalsIgnoreCase(tipo)) {
                BoolRecord record = objectMapper.readValue(jsonBody, BoolRecord.class);
                return createRecordAux(mid, record);
            } else if ("Num".equalsIgnoreCase(tipo)) {
                NumRecord record = objectMapper.readValue(jsonBody, NumRecord.class);
                return createRecordAux(mid, record);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de meta no soportado: " + tipo);
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON malformado o inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    @DeleteMapping("/{mid}/registros/{rid}")
    public ResponseEntity<?> deleteRegistro(@RequestHeader("Authorization") String authorizationHeader,
                                            @PathVariable("mid") String mid,
                                            @PathVariable("rid") String rid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        if (persistenceService.deleteRecord(mid,rid)) return ResponseEntity.ok("Registro eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar el Registro");
    }

    // Funciones Auxiliares

    private ResponseEntity<?> postMeta(String authorizationHeader, Goal goal) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document doc = persistenceService.createGoal(goal, uid);
        if (doc != null) return ResponseEntity.status(HttpStatus.CREATED).body(doc.toJson());
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear la meta");
    }

    private ResponseEntity<?> patchMeta(String authorizationHeader, String mid, UpdateGoalDTO update) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        String uid = validation.getBody().toString();

        Document doc = persistenceService.updateGoal(update, uid, mid);
        if (doc != null) return ResponseEntity.ok(doc.toJson());
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido actualizar la meta");
    }

    private ResponseEntity<?> createRecordAux(String mid, Record record) {
        Document doc = persistenceService.createRecord(record, mid);
        if (doc != null) {
            return ResponseEntity.ok(doc.toJson());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear el registro");
        }
    }
}