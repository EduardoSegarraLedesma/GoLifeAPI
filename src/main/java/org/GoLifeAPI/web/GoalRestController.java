package org.GoLifeAPI.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.GoLifeAPI.service.interfaces.IGoalService;
import org.GoLifeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLifeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLifeAPI.model.Enums;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/metas")
public class GoalRestController {

    private final IGoalService goalService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public GoalRestController(IGoalService goalService,
                              ObjectMapper objectMapper,
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
    public ResponseEntity<ResponseUserDTO> postMetaBool(@AuthenticationPrincipal String uid,
                                                        @Valid @RequestBody CreateBoolGoalDTO createBoolGoalDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createBoolGoal(createBoolGoalDTO, uid));
    }

    @PostMapping("/num")
    public ResponseEntity<ResponseUserDTO> postMetaNum(@AuthenticationPrincipal String uid,
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
    public ResponseEntity<ResponseUserDTO> postMetaFinalizar(@AuthenticationPrincipal String uid,
                                                             @PathVariable("mid") String mid) {
        return ResponseEntity.ok(goalService.finalizeGoal(uid, mid));
    }

    @PatchMapping("/{mid}")
    public ResponseEntity<ResponseUserDTO> patchMeta(@AuthenticationPrincipal String uid,
                                                     @PathVariable String mid,
                                                     @RequestParam("tipo") Enums.Tipo tipo,
                                                     @RequestBody JsonNode jsonBody) {
        try {
            if (tipo.toString().equalsIgnoreCase("Bool")) {
                PatchBoolGoalDTO boolDto = objectMapper.treeToValue(jsonBody, PatchBoolGoalDTO.class);
                validateDTO(boolDto);
                return ResponseEntity.ok(goalService.updateBoolGoal(boolDto, uid, mid));
            } else if (tipo.toString().equalsIgnoreCase("Num")) {
                PatchNumGoalDTO numDto = objectMapper.treeToValue(jsonBody, PatchNumGoalDTO.class);
                validateDTO(numDto);
                return ResponseEntity.ok(goalService.updateNumGoal(numDto, uid, mid));
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de meta no soportado");
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cuerpo de la petición malformado o invalido", e);
        }
    }

    @DeleteMapping(path = "/{mid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseUserStatsDTO> deleteMeta(@AuthenticationPrincipal String uid,
                                                           @PathVariable("mid") String mid) {
        return ResponseEntity.ok(goalService.deleteGoal(uid, mid));
    }

    // Auxiliary Methods

    private <T> void validateDTO(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }
}