package org.GoLIfeAPI.controllers.REST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.GoLIfeAPI.Models.Records.BoolRecord;
import org.GoLIfeAPI.Models.Records.NumRecord;
import org.GoLIfeAPI.Models.Records.Record;
import org.GoLIfeAPI.controllers.Persistence.GoalPersistenceController;
import org.GoLIfeAPI.controllers.Persistence.RecordPersistenceController;
import org.GoLIfeAPI.services.FirebaseService;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metas/{mid}/registros")
public class RecordRestController extends BaseRestController {

    private final RecordPersistenceController recordPersistenceController;
    private final GoalPersistenceController goalPersistenceController;

    public RecordRestController(FirebaseService firebaseService,
                                RecordPersistenceController recordPersistenceController,
                                GoalPersistenceController goalPersistenceController) {
        super(firebaseService);
        this.recordPersistenceController = recordPersistenceController;
        this.goalPersistenceController = goalPersistenceController;
    }

    @PostMapping()
    public ResponseEntity<?> postRegistro(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable("mid") String mid,
                                          @RequestBody String jsonBody) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;

        Document meta = goalPersistenceController.read(mid);
        if (meta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Meta no encontrada");
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de meta no soportado");
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON malformado o inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
        }
    }

    @DeleteMapping("/{rid}")
    public ResponseEntity<?> deleteRegistro(@RequestHeader("Authorization") String authorizationHeader,
                                            @PathVariable("mid") String mid,
                                            @PathVariable("rid") String rid) {
        ResponseEntity<?> validation = validateToken(authorizationHeader);
        if (!validation.getStatusCode().is2xxSuccessful()) return validation;
        if (recordPersistenceController.delete(mid, rid)) return ResponseEntity.ok("Registro eliminado exitosamente");
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido eliminar el Registro");
    }

    // Funciones Auxiliares

    private ResponseEntity<?> createRecordAux(String mid, Record record) {
        Document doc = recordPersistenceController.create(record, mid);
        if (doc != null) {
            return ResponseEntity.ok(doc.toJson());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se ha podido crear el registro");
        }
    }
}
