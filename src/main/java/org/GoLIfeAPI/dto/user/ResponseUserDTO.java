package org.GoLIfeAPI.dto.user;

import org.GoLIfeAPI.dto.goal.ResponsePartialGoalDTO;

import java.util.List;

public class ResponseUserDTO {

    private String nombre;
    private String apellidos;
    private List<ResponsePartialGoalDTO> metas;
    private ResponseUserStatsDTO estadisticas;

    public ResponseUserDTO(String apellidos, String nombre,
                           List<ResponsePartialGoalDTO> metas, ResponseUserStatsDTO estadisticas) {
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.metas = metas;
        this.estadisticas = estadisticas;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<ResponsePartialGoalDTO> getMetas() {
        return metas;
    }

    public ResponseUserStatsDTO getEstadisticas() {
        return estadisticas;
    }
}