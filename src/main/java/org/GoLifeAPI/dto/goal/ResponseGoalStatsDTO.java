package org.GoLifeAPI.dto.goal;

public class ResponseGoalStatsDTO {

    private Boolean valorAlcanzado;
    private Boolean acabaDeAlcanzarse;
    private String fechaFin;

    public ResponseGoalStatsDTO(Boolean valorAlcanzado, Boolean acabaDeAlcanzarse, String fechaFin) {
        this.valorAlcanzado = valorAlcanzado;
        this.acabaDeAlcanzarse = acabaDeAlcanzarse;
        this.fechaFin = fechaFin;
    }

    public Boolean getValorAlcanzado() {
        return valorAlcanzado;
    }

    public Boolean getAcabaDeAlcanzarse() {
        return acabaDeAlcanzarse;
    }

    public String getFechaFin() {
        return fechaFin;
    }
}