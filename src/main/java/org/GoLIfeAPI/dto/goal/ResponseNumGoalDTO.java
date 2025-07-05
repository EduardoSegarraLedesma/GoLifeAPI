package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.record.NumRecord;

import java.util.List;

public class ResponseNumGoalDTO extends ResponseGoalDTO {

    private Float valorObjetivo;
    private String unidad;
    private List<NumRecord> registros;

    public ResponseNumGoalDTO() {
        super();
    }

    public ResponseNumGoalDTO(String _id, String nombre, Goal.Tipo tipo,
                              String descripcion, String fecha, Boolean finalizado,
                              int duracionValor, Goal.Duracion duracionUnidad, List<NumRecord> registros,
                              Float valorObjetivo, String unidad) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
        this.registros = registros;
    }

    public List<NumRecord> getRegistros() {
        return registros;
    }

    public void setRegistros(List<NumRecord> registros) {
        this.registros = registros;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Float getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Float valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }
}