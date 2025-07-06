package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.goal.ResponsePartialGoalDTO;
import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.exception.NotFoundException;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.persistence.UserPersistenceController;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserPersistenceController userPersistenceController;
    private final StatsService statsService;

    @Autowired
    public UserService(UserPersistenceController userPersistenceController,
                       StatsService statsService) {
        this.userPersistenceController = userPersistenceController;
        this.statsService = statsService;
    }

    public ResponseUserDTO createUser(CreateUserDTO dto, String uid) {
        return mapToResponseUserDTO(userPersistenceController.create(dto.toEntity(uid).toDocument(), uid));
    }

    public ResponseUserDTO getUser(String uid) {
        Document userDoc = userPersistenceController.read(uid);
        if (userDoc == null) throw new NotFoundException("No se ha encontrado al usuario");
        else return mapToResponseUserDTO(userDoc);
    }

    public ResponseUserDTO updateUser(PatchUserDTO dto, String uid) {
        return mapToResponseUserDTO(userPersistenceController.update(dto.toDocument(), uid));
    }

    public void deleteUser(String uid) {
        userPersistenceController.delete(uid);
    }

    private ResponseUserDTO mapToResponseUserDTO(Document doc) {
        List<Document> partialGoals = doc.getList("metas", Document.class);
        List<ResponsePartialGoalDTO> partialGoalDTOs = Collections.emptyList();
        if (partialGoals != null) {
            partialGoalDTOs = partialGoals.stream().map(d -> {
                ObjectId id = d.getObjectId("_id");
                String nombre = d.getString("nombre");
                Goal.Tipo tipo = Goal.Tipo.valueOf(d.getString("tipo"));
                String fecha = d.getString("fecha");
                Boolean finalizado = d.getBoolean("finalizado");
                int duracionValor = d.getInteger("duracionValor");
                Goal.Duracion durUnidad = Goal.Duracion.valueOf(d.getString("duracionUnidad"));
                return new ResponsePartialGoalDTO(
                        id,
                        nombre,
                        tipo,
                        fecha,
                        finalizado,
                        duracionValor,
                        durUnidad
                );
            }).collect(Collectors.toList());
        }
        return new ResponseUserDTO(doc.getString("nombre"), doc.getString("apellidos"),
                partialGoalDTOs, statsService.mapEmbeddedToResponseUserStatsDTO(doc));
    }
}