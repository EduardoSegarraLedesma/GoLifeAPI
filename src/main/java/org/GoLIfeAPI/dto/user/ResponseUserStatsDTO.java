package org.GoLIfeAPI.dto.user;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ResponseUserStatsDTO {

    private int totalMetas;
    private int totalMetasFinalizadas;
    private BigDecimal porcentajeFinalizadas;

    public ResponseUserStatsDTO(int totalMetas, int totalMetasFinalizadas) {
        this.totalMetas = Math.max(totalMetas, 0);
        this.totalMetasFinalizadas = Math.max(totalMetasFinalizadas, 0);
        calculatePorcentajeFinalizadas();
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

    private void calculatePorcentajeFinalizadas() {
        if (totalMetas > 0 && totalMetasFinalizadas > 0) {
            this.porcentajeFinalizadas = BigDecimal.valueOf(totalMetasFinalizadas)
                    .divide(BigDecimal.valueOf(totalMetas), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.porcentajeFinalizadas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}