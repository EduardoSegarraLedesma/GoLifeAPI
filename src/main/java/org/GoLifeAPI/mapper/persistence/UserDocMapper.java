package org.GoLifeAPI.mapper.persistence;

import org.GoLifeAPI.model.goal.PartialGoal;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.model.user.UserStats;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDocMapper {

    private final GoalDocMapper goalDocMapper;

    public UserDocMapper(GoalDocMapper goalDocMapper) {
        this.goalDocMapper = goalDocMapper;
    }

    // Map POJOs to Docs

    public Document mapUsertoDoc(User user) {
        Document doc = new Document("uid", user.getUid())
                .append("nombre", user.getNombre())
                .append("apellidos", user.getApellidos());
        if (user.getMetas() != null) {
            List<Document> metasDocs = user.getMetas()
                    .stream()
                    .map(partialGoal -> goalDocMapper.mapPartialGoalToDoc(partialGoal))
                    .collect(Collectors.toList());
            doc.append("metas", metasDocs);
        } else {
            doc.append("metas", List.of());
        }
        doc.append("estadisticas", mapUserStatsToDoc(user.getEstadisticas()));
        return doc;
    }

    private Document mapUserStatsToDoc(UserStats userStats) {
        return new Document("totalMetas", userStats.getTotalMetas())
                .append("totalMetasFinalizadas", userStats.getTotalMetasFinalizadas());
    }

    // Map DOCs to POJOs

    public User mapDocToUser(Document userDoc) {
        if(userDoc == null || userDoc.isEmpty()) return null;
        List<Document> partialGoalsDoc = userDoc.getList("metas", Document.class);
        List<PartialGoal> partialGoals = Collections.emptyList();
        if (partialGoalsDoc != null) {
            partialGoals = partialGoalsDoc
                    .stream()
                    .map(Goal -> goalDocMapper.mapDocToPartialGoal(Goal))
                    .collect(Collectors.toList());
        }
        return new User(
                userDoc.getString("uid"),
                userDoc.getString("nombre"),
                userDoc.getString("apellidos"),
                partialGoals,
                mapDocToUserStats(userDoc));
    }

    public UserStats mapDocToUserStats(Document userDoc) {
        Document statsDoc = userDoc.get("estadisticas", Document.class);
        int totalMetas = statsDoc.getInteger("totalMetas");
        int totalMetasFinalizadas = statsDoc.getInteger("totalMetasFinalizadas");
        return new UserStats(totalMetas, totalMetasFinalizadas);
    }
}