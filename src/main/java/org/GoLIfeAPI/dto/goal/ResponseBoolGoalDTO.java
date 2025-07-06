package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLIfeAPI.model.goal.Goal;

import java.util.List;

public class ResponseBoolGoalDTO extends ResponseGoalDTO {

    private List<ResponseBoolRecordDTO> registros;

    public ResponseBoolGoalDTO(String _id, String nombre, Goal.Tipo tipo,
                               String descripcion, String fecha, Boolean finalizado,
                               int duracionValor, Goal.Duracion duracionUnidad, List<ResponseBoolRecordDTO> registros) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        this.registros = registros;
    }

    public List<ResponseBoolRecordDTO> getRegistros() {
        return registros;
    }
}