package org.GoLIfeAPI.model.user;

import org.bson.Document;

public class UserStats {

    private int totalMetas;
    private int totalMetasFinalizadas;

    public UserStats() {
        this.totalMetas = 0;
        this.totalMetasFinalizadas = 0;
    }

    public UserStats(int totalMetas, int totalMetasFinalizadas) {
        this.totalMetas = Math.max(totalMetas, 0);
        this.totalMetasFinalizadas = Math.max(totalMetasFinalizadas, 0);
    }


    public Document toDocument() {
        Document doc = new Document();
        doc.append("totalMetas", totalMetas);
        doc.append("totalMetasFinalizadas", totalMetasFinalizadas);
        return doc;
    }

    public int getTotalMetas() {
        return totalMetas;
    }

    public void setTotalMetas(int totalMetas) {
        if (totalMetas > 0) {
            this.totalMetas = totalMetas;
        }
    }

    public int getTotalMetasFinalizadas() {
        return totalMetasFinalizadas;
    }

    public void setTotalMetasFinalizadas(int totalMetasFinalizadas) {
        if (totalMetasFinalizadas > 0) {
            this.totalMetasFinalizadas = totalMetasFinalizadas;
        }
    }

}