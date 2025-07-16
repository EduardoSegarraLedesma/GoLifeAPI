package org.GoLIfeAPI.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.GoLIfeAPI.service.interfaces.IRecordService;
import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.model.Enums;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/metas/{mid}/registros")
public class RecordRestController {

    private final IRecordService recordService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public RecordRestController(IRecordService recordService,
                                ObjectMapper objectMapper,
                                Validator validator) {
        this.recordService = recordService;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);
        this.validator = validator;
    }

    @PostMapping()
    public ResponseEntity<?> postRegistro(@AuthenticationPrincipal String uid,
                                          @PathVariable("mid") String mid,
                                          @RequestParam("tipo") Enums.Tipo tipo,
                                          @RequestBody JsonNode jsonBody) {
        try {
            if (tipo.toString().equalsIgnoreCase("Bool")) {
                CreateBoolRecordDTO boolRecord = objectMapper.treeToValue(jsonBody, CreateBoolRecordDTO.class);
                validateDTO(boolRecord);
                ResponseBoolGoalDTO updated = recordService.createBoolRecord(boolRecord, uid, mid);
                return ResponseEntity.ok(updated);
            } else if (tipo.toString().equalsIgnoreCase("Num")) {
                CreateNumRecordDTO numRecord = objectMapper.treeToValue(jsonBody, CreateNumRecordDTO.class);
                validateDTO(numRecord);
                ResponseNumGoalDTO updated = recordService.createNumRecord(numRecord, uid, mid);
                return ResponseEntity.ok(updated);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de meta no soportado");
            }
        } catch (JsonParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cuerpo de la petición malformado", e);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cuerpo de la petición malformado", e);
        }
    }

    @DeleteMapping("/{fecha}")
    public ResponseEntity<?> deleteRegistro(@AuthenticationPrincipal String uid,
                                            @PathVariable("mid") String mid,
                                            @PathVariable("fecha") LocalDate fecha) {
        recordService.deleteRecord(uid, mid, fecha);
        return ResponseEntity.ok("Registro eliminado exitosamente");
    }

    // Auxiliary Methods

    private <T> void validateDTO(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }
}