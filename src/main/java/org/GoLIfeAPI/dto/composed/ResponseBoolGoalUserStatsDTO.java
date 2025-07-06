package org.GoLIfeAPI.dto.composed;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;

public class ResponseBoolGoalUserStatsDTO {

    private ResponseBoolGoalDTO meta;
    private ResponseUserStatsDTO estadisticasUsuario;

    public ResponseBoolGoalUserStatsDTO(ResponseBoolGoalDTO meta, ResponseUserStatsDTO estadisticasUsuario) {
        this.meta = meta;
        this.estadisticasUsuario = estadisticasUsuario;
    }


    public ResponseBoolGoalDTO getMeta() {
        return meta;
    }

    public ResponseUserStatsDTO getEstadisticasUsuario() {
        return estadisticasUsuario;
    }
}