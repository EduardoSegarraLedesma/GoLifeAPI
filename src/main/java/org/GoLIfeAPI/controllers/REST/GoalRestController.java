package org.GoLIfeAPI.controllers.REST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.GoLIfeAPI.controllers.Persistence.GoalPersistenceController;
import org.GoLIfeAPI.models.DTOs.UpdateBoolGoalDTO;
import org.GoLIfeAPI.models.DTOs.UpdateGoalDTO;
import org.GoLIfeAPI.models.DTOs.UpdateNumGoalDTO;
import org.GoLIfeAPI.models.Goals.BoolGoal;
import org.GoLIfeAPI.models.Goals.Goal;
import org.GoLIfeAPI.models.Goals.NumGoal;
import org.GoLIfeAPI.utils.GoalValidationHelper;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metas")
public class GoalRestController {

    private final GoalPersistenceController goalPersistenceController;
    private final GoalValidationHelper goalValidationHelper;

    public GoalRestController(GoalPersistenceController goalPersistenceController,
                              GoalValidationHelper goalValidationHelper) {
        this.goalPersistenceController = goalPersistenceController;
        this.goalValidationHelper = goalValidationHelper;
    }

    @PostMapping("/bool")
    public ResponseEntity<?> postMetaBool(@AuthenticationPrincipal String uid,
                                          @Valid @RequestBody BoolGoal goal) {
        return postMeta(uid, goal);
    }

    @PostMapping("/num")
    public ResponseEntity<?> postMetaNum(@AuthenticationPrincipal String uid,
                                         @Valid @RequestBody NumGoal goal) {
        return postMeta(uid, goal);
    }

    @GetMapping("/{mid}")
    public ResponseEntity<?> getMeta(@AuthenticationPrincipal String uid,
                                     @PathVariable("mid") String mid) {
        Document goal = goalValidationHelper.validateAndGetGoal(uid, mid);
        return ResponseEntity.ok(goal.toJson());
    }

    @PostMapping("/{mid}/finalizar")
    public ResponseEntity<?> postMetaFinalizar(@AuthenticationPrincipal String uid,
                                               @PathVariable("mid") String mid) {
        goalValidationHelper.validateAndGetGoal(uid, mid);
        UpdateBoolGoalDTO update = new UpdateBoolGoalDTO();
        update.setFinalizado(true);
        return patchMetaAux(uid, mid, update);
    }

    @PatchMapping("/{mid}")
    public ResponseEntity<?> patchMeta(@AuthenticationPrincipal String uid,
                                       @PathVariable("mid") String mid,
                                       @RequestBody String jsonBody) {
        try {
            Document goal = goalValidationHelper.validateAndGetGoal(uid, mid);
            String tipo = goal.getString("tipo");
            ObjectMapper objectMapper = goalValidationHelper.getObjectMapper();
            if ("Bool".equalsIgnoreCase(tipo)) {
                UpdateBoolGoalDTO updateDTO = objectMapper.readValue(jsonBody, UpdateBoolGoalDTO.class);
                goalValidationHelper.validateDTO(updateDTO);
                return patchMetaAux(uid, mid, updateDTO);
            } else if ("Num".equalsIgnoreCase(tipo)) {
                UpdateNumGoalDTO updateDTO = objectMapper.readValue(jsonBody, UpdateNumGoalDTO.class);
                goalValidationHelper.validateDTO(updateDTO);
                return patchMetaAux(uid, mid, updateDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de meta no soportado");
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON malformado o inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    @DeleteMapping("/{mid}")
    public ResponseEntity<?> deleteMeta(@AuthenticationPrincipal String uid,
                                        @PathVariable("mid") String mid) {
        goalValidationHelper.validateAndGetGoal(uid, mid);
        if (goalPersistenceController.delete(mid, uid)) return ResponseEntity.ok("Meta eliminado exitosamente");
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar la Meta");
    }

    // Funciones Auxiliares

    private ResponseEntity<?> postMeta(String uid, Goal goal) {
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