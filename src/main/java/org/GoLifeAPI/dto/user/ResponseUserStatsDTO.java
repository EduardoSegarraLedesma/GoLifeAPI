package org.GoLifeAPI.dto.user;

import java.math.BigDecimal;

public class ResponseUserStatsDTO {

    private int totalMetas;
    private int totalMetasFinalizadas;
    private BigDecimal porcentajeFinalizadas;

    public ResponseUserStatsDTO(int totalMetas, int totalMetasFinalizadas, BigDecimal porcentajeFinalizadas) {
        this.totalMetas = totalMetas;
        this.totalMetasFinalizadas = totalMetasFinalizadas;
        this.porcentajeFinalizadas = porcentajeFinalizadas;
    }

    public int getTotalMetas() {
        return totalMetas;
    }

    public int getTotalMetasFinalizadas() {
        return totalMetasFinalizadas;
    }

    public BigDecimal getPorcentajeFinalizadas() {
        return porcentajeFinalizadas;
    }
}