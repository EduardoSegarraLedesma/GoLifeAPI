package org.GoLIfeAPI.mapper;

import org.GoLIfeAPI.dto.goal.ResponsePartialGoalDTO;
import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.PatchUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final GoalMapper goalMapper;

    public UserMapper(GoalMapper goalMapper) {
        this.goalMapper = goalMapper;
    }

    // Map Input DTOs to POJOs

    public User mapCreateUserDtoToUser(CreateUserDTO newUserDto, String uid) {
        return new User(uid, newUserDto.getApellidos(), newUserDto.getNombre());
    }

    // Map Input DTOs to Docs

    public Document mapPatchUserDtoToDoc(PatchUserDTO dto) {
        Document doc = new Document();
        String name = dto.getNombre();
        String surename = dto.getApellidos();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        if (surename != null && !surename.isBlank()) doc.append("apellidos", surename);
        return doc;
    }

    // Map POJOs to Docs

    public Document mapUsertoUserDoc(User user) {
        Document doc = new Document("uid", user.getUid())
                .append("nombre", user.getNombre())
                .append("apellidos", user.getApellidos());
        if (user.getMetas() != null) {
            List<Document> metasDocs = user.getMetas()
                    .stream()
                    .map(Goal -> goalMapper.mapGoalToParcialDoc(Goal))
                    .collect(Collectors.toList());
            doc.append("metas", metasDocs);
        } else {
            doc.append("metas", List.of());
        }
        doc.append("estadisticas", mapUserStatsToUserStatsDoc(user.getEstadisticas()));
        return doc;
    }

    private Document mapUserStatsToUserStatsDoc(UserStats userStats) {
        Document doc = new Document();
        doc.append("totalMetas", userStats.getTotalMetas());
        doc.append("totalMetasFinalizadas", userStats.getTotalMetasFinalizadas());
        return doc;
    }

    // Map Docs to Output DTOs

    public ResponseUserDTO mapUserDocToResponseUserDTO(Document newDoc) {
        List<Document> partialGoals = newDoc.getList("metas", Document.class);
        List<ResponsePartialGoalDTO> partialGoalDTOs = Collections.emptyList();
        if (partialGoals != null) {
            partialGoalDTOs = partialGoals
                    .stream()
                    .map(Goal -> goalMapper.mapGoalDocToPartialGoalDTO(Goal))
                    .collect(Collectors.toList());
        }
        return new ResponseUserDTO(
                newDoc.getString("nombre"),
                newDoc.getString("apellidos"),
                partialGoalDTOs,
                mapUserDocToResponseUserStatsDTO(newDoc));
    }

    public ResponseUserStatsDTO mapUserDocToResponseUserStatsDTO(Document doc) {
        Document statsDoc = doc.get("estadisticas", Document.class);
        int totalMetas = statsDoc.getInteger("totalMetas");
        int totalMetasFinalizadas = statsDoc.getInteger("totalMetasFinalizadas");
        return new ResponseUserStatsDTO(
                totalMetas,
                totalMetasFinalizadas,
                calculatePorcentajeFinalizadas(totalMetas, totalMetasFinalizadas));
    }

    private BigDecimal calculatePorcentajeFinalizadas(int totalMetas, int totalMetasFinalizadas) {
        if (totalMetas > 0 && totalMetasFinalizadas > 0) {
            return BigDecimal.valueOf(totalMetasFinalizadas)
                    .divide(BigDecimal.valueOf(totalMetas), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}