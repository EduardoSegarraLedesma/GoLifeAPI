package org.GoLIfeAPI.controllers.REST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.GoLIfeAPI.models.Records.BoolRecord;
import org.GoLIfeAPI.models.Records.NumRecord;
import org.GoLIfeAPI.models.Records.Record;
import org.GoLIfeAPI.controllers.Persistence.RecordPersistenceController;
import org.GoLIfeAPI.utils.GoalValidationHelper;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/metas/{mid}/registros")
public class RecordRestController {

    private final RecordPersistenceController recordPersistenceController;
    private final GoalValidationHelper goalValidationHelper;


    public RecordRestController(RecordPersistenceController recordPersistenceController,
                                GoalValidationHelper goalValidationHelper) {
        this.recordPersistenceController = recordPersistenceController;
        this.goalValidationHelper = goalValidationHelper;
    }

    @PostMapping()
    public ResponseEntity<?> postRegistro(HttpServletRequest request,
                                          @PathVariable("mid") String mid,
                                          @RequestBody String jsonBody) {
        String uid = (String) request.getAttribute("uid");
        try {
            Document goal = goalValidationHelper.validateAndGetGoal(uid, mid);
            String tipo = goal.getString("tipo");
            ObjectMapper objectMapper = goalValidationHelper.getObjectMapper();
            if ("Bool".equalsIgnoreCase(tipo)) {
                BoolRecord record = objectMapper.readValue(jsonBody, BoolRecord.class);
                return createRecordAux(mid, goal, record);
            } else if ("Num".equalsIgnoreCase(tipo)) {
                NumRecord record = objectMapper.readValue(jsonBody, NumRecord.class);
                return createRecordAux(mid, goal, record);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de meta no soportado");
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("JSON malformado o inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    @DeleteMapping("/{fecha}")
    public ResponseEntity<?> deleteRegistro(HttpServletRequest request,
                                            @PathVariable("mid") String mid,
                                            @PathVariable("fecha") LocalDate fecha) {
        String uid = (String) request.getAttribute("uid");
        goalValidationHelper.validateAndGetGoal(uid, mid);
        if (recordPersistenceController.delete(mid, fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            return ResponseEntity.ok("Registro eliminado exitosamente");
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar el Registro");
    }

    // Funciones Auxiliares

    private ResponseEntity<?> createRecordAux(String mid, Document goal, Record record) {
        goalValidationHelper.validateDTO(record);

        LocalDate goalDate = LocalDate.parse(goal.getString("fecha"), DateTimeFormatter.ISO_LOCAL_DATE);
        if (record.getFecha().isBefore(goalDate))
            return ResponseEntity.badRequest().body("La fecha del registro no puede ser anterior a la fecha inicial de la meta");

        List<Document> records = (List<Document>) goal.get("registros");
        String newDate = record.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE);
        boolean exists = records.stream().anyMatch(reg -> newDate.equals(reg.getString("fecha")));
        if (exists)
            return ResponseEntity.badRequest().body("Ya existe un registro para la fecha: " + newDate);

        Document doc = recordPersistenceController.create(record, mid);
        if (doc != null) {
            return ResponseEntity.ok(doc.toJson());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear el registro");
        }
    }
}