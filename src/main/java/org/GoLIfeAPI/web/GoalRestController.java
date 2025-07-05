package org.GoLIfeAPI.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/metas")
public class GoalRestController {

    private final GoalService goalService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public GoalRestController(GoalService goalService, ObjectMapper objectMapper,
                              Validator validator) {
        this.goalService = goalService;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);
        this.validator = validator;
    }

    @PostMapping("/bool")
    public ResponseEntity<ResponseBoolGoalDTO> postMetaBool(@AuthenticationPrincipal String uid,
                                                            @Valid @RequestBody CreateBoolGoalDTO createBoolGoalDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createBoolGoal(createBoolGoalDTO, uid));
    }

    @PostMapping("/num")
    public ResponseEntity<ResponseNumGoalDTO> postMetaNum(@AuthenticationPrincipal String uid,
                                                          @Valid @RequestBody CreateNumGoalDTO createNumGoalDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createNumGoal(createNumGoalDTO, uid));
    }

    @GetMapping("/{mid}")
    public ResponseEntity<?> getMeta(@AuthenticationPrincipal String uid,
                                     @PathVariable("mid") String mid) {
        return ResponseEntity.ok(goalService.getGoal(uid, mid));
    }

    @PostMapping("/{mid}/finalizar")
    public ResponseEntity<?> postMetaFinalizar(@AuthenticationPrincipal String uid,
                                               @PathVariable("mid") String mid) {
        return ResponseEntity.ok(goalService.finalizeGoal(uid, mid));
    }

    @PatchMapping("/{mid}")
    public ResponseEntity<?> patchMeta(@AuthenticationPrincipal String uid,
                                       @PathVariable String mid,
                                       @RequestParam("tipo") Goal.Tipo tipo,
                                       @RequestBody JsonNode jsonBody) {
        try {
            if (tipo.toString().equalsIgnoreCase("Bool")) {
                PatchBoolGoalDTO boolDto = objectMapper.treeToValue(
                        jsonBody, PatchBoolGoalDTO.class);
                validateDTO(boolDto);
                ResponseBoolGoalDTO updated = goalService.updateBoolGoal(boolDto, uid, mid);
                return ResponseEntity.ok(updated);
            } else if (tipo.toString().equalsIgnoreCase("Num")) {
                PatchNumGoalDTO numDto = objectMapper.treeToValue(
                        jsonBody, PatchNumGoalDTO.class);
                validateDTO(numDto);
                ResponseNumGoalDTO updated = goalService.updateNumGoal(numDto, uid, mid);
                return ResponseEntity.ok(updated);
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de meta no soportado");
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cuerpo de la petición malformado", e);
        }
    }

    @DeleteMapping("/{mid}")
    public ResponseEntity<?> deleteMeta(@AuthenticationPrincipal String uid,
                                        @PathVariable("mid") String mid) {
        goalService.deleteGoal(uid, mid);
        return ResponseEntity.ok("Meta eliminado exitosamente");
    }

    // Auxiliary Methods

    private <T> void validateDTO(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }
}