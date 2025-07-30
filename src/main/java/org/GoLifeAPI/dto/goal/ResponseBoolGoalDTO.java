package org.GoLifeAPI.dto.goal;

import org.GoLifeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLifeAPI.model.Enums;

import java.util.List;

public class ResponseBoolGoalDTO extends ResponseGoalDTO {

    private List<ResponseBoolRecordDTO> registros;

    public ResponseBoolGoalDTO(String _id, String nombre, Enums.Tipo tipo,
                               String descripcion, String fecha, Boolean finalizado,
                               int duracionValor, Enums.Duracion duracionUnidad, ResponseGoalStatsDTO estadisticas,
                               List<ResponseBoolRecordDTO> registros) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad, estadisticas);
        this.registros = registros;
    }

    public List<ResponseBoolRecordDTO> getRegistros() {
        return registros;
    }
}