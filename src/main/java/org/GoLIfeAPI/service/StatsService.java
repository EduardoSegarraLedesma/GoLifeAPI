package org.GoLIfeAPI.service;

import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    public Document getUpdatedUserStatsDoc(int deltaTotalGoals, int deltaTotalFinalizedGoals) {
        return new Document()
                .append("totalMetas", deltaTotalGoals)
                .append("totalMetasFinalizadas", deltaTotalFinalizedGoals);
    }

    public ResponseUserStatsDTO mapToResponseUserStatsDTO(Document doc) {
        return new ResponseUserStatsDTO(
                doc.getInteger("totalMetas"),
                doc.getInteger("totalMetasFinalizadas"));
    }

    public ResponseUserStatsDTO mapEmbeddedToResponseUserStatsDTO(Document doc) {
        Document statsDoc = doc.get("estadisticas", Document.class);
        return new ResponseUserStatsDTO(
                statsDoc.getInteger("totalMetas"),
                statsDoc.getInteger("totalMetasFinalizadas"));
    }
}