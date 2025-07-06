package org.GoLIfeAPI.dto.composed;

import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;

public class ResponseNumGoalUserStatsDTO {

    private ResponseNumGoalDTO meta;
    private ResponseUserStatsDTO estadisticasUsuario;

    public ResponseNumGoalUserStatsDTO(ResponseNumGoalDTO meta, ResponseUserStatsDTO estadisticasUsuario) {
        this.meta = meta;
        this.estadisticasUsuario = estadisticasUsuario;
    }

    public ResponseNumGoalDTO getMeta() {
        return meta;
    }

    public ResponseUserStatsDTO getEstadisticasUsuario() {
        return estadisticasUsuario;
    }
}