package org.GoLifeAPI.dto.goal;

import org.GoLifeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLifeAPI.model.Enums;

import java.util.List;

public class ResponseNumGoalDTO extends ResponseGoalDTO {

    private Double valorObjetivo;
    private String unidad;
    private List<ResponseNumRecordDTO> registros;

    public ResponseNumGoalDTO(String _id, String nombre, Enums.Tipo tipo,
                              String descripcion, String fecha, Boolean finalizado,
                              int duracionValor, Enums.Duracion duracionUnidad, ResponseGoalStatsDTO estadisticas,
                              List<ResponseNumRecordDTO> registros, Double valorObjetivo, String unidad) {
        super(_id, nombre, tipo, descripcion, fecha, finalizado, duracionValor, duracionUnidad, estadisticas);
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

    public Double getValorObjetivo() {
        return valorObjetivo;
    }
}