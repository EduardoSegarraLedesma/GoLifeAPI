package org.GoLifeAPI.dto.goal;

public class ResponseGoalStatsDTO {

    private Boolean tienePrimerRegistro;
    private Boolean acabaDeCrearsePrimerRegistro;
    private String fechaFin;

    public ResponseGoalStatsDTO(Boolean tienePrimerRegistro, Boolean acabaDeCrearse, String fechaFin) {
        this.tienePrimerRegistro = tienePrimerRegistro;
        this.acabaDeCrearsePrimerRegistro = acabaDeCrearse;
        this.fechaFin = fechaFin;
    }

    public Boolean getTienePrimerRegistro() {
        return tienePrimerRegistro;
    }

    public Boolean getAcabaDeCrearsePrimerRegistro() {
        return acabaDeCrearsePrimerRegistro;
    }

    public String getFechaFin() {
        return fechaFin;
    }
}