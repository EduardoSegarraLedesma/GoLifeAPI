package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLIfeAPI.model.goal.Goal;

import java.util.List;

public class ResponseNumGoalDTO extends ResponseGoalDTO {

    private Float valorObjetivo;
    private String unidad;
    private List<ResponseNumRecordDTO> registros;

    public ResponseNumGoalDTO(String _id, String nombre, Goal.Tipo tipo,
                              String descripcion, String fecha, Boolean finalizado,
                              int duracionValor, Goal.Duracion duracionUnidad, List<ResponseNumRecordDTO> registros,
                              Float valorObjetivo, String unidad) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
        this.registros = registros;
    }

    public List<ResponseNumRecordDTO> getRegistros() {
        return registros;
    }

    public String getUnidad() {
        return unidad;
    }

    public Float getValorObjetivo() {
        return valorObjetivo;
    }
}