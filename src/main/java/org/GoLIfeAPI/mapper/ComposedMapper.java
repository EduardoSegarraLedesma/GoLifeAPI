package org.GoLIfeAPI.mapper;

import org.GoLIfeAPI.dto.composed.ResponseBoolGoalUserStatsDTO;
import org.GoLIfeAPI.dto.composed.ResponseNumGoalUserStatsDTO;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ComposedMapper {

    private final UserMapper userMapper;
    private final GoalMapper goalMapper;

    public ComposedMapper(UserMapper userMapper, GoalMapper goalMapper) {
        this.userMapper = userMapper;
        this.goalMapper = goalMapper;
    }

    // Map Docs to Output DTOs

    public Object mapDocToComposedResponseGoalDTO(Document composedDoc, Document oldDoc) {
        String type = composedDoc.get("meta", Document.class).getString("tipo");
        if (type.equals("Bool"))
            return mapDocToComposedResponseBoolGoalDTO(composedDoc, oldDoc);
        else if (type.equals("Num"))
            return mapDocToComposedResponseNumGoalDTO(composedDoc, oldDoc);
        else
            throw new RuntimeException("Error interno", new Throwable());
    }

    public ResponseBoolGoalUserStatsDTO mapDocToComposedResponseBoolGoalDTO(Document composedDoc, Document oldDoc) {
        Document newDoc = composedDoc.get("meta", Document.class);
        return new ResponseBoolGoalUserStatsDTO(
                goalMapper.mapGoalDocToResponseBoolGoalDTO(newDoc, oldDoc),
                userMapper.mapUserDocToResponseUserStatsDTO(composedDoc));
    }

    public ResponseNumGoalUserStatsDTO mapDocToComposedResponseNumGoalDTO(Document composedDoc, Document oldDoc) {
        Document newDoc = composedDoc.get("meta", Document.class);
        return new ResponseNumGoalUserStatsDTO(
                goalMapper.mapGoalDocToResponseNumGoalDTO(newDoc, oldDoc),
                userMapper.mapUserDocToResponseUserStatsDTO(composedDoc));
    }
}
