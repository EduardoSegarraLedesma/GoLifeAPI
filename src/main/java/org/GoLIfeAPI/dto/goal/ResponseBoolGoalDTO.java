package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.record.BoolRecord;

import java.util.List;

public class ResponseBoolGoalDTO extends ResponseGoalDTO {

    private List<BoolRecord> registros;

    public ResponseBoolGoalDTO() {
        super();
    }

    public ResponseBoolGoalDTO(String _id, String nombre, Goal.Tipo tipo,
                               String descripcion, String fecha, Boolean finalizado,
                               int duracionValor, Goal.Duracion duracionUnidad, List<BoolRecord> registros) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        this.registros = registros;
    }

    public List<BoolRecord> getRegistros() {
        return registros;
    }

    public void setRegistros(List<BoolRecord> registros) {
        this.registros = registros;
    }
}