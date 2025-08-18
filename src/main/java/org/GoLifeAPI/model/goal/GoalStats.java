package org.GoLifeAPI.model.goal;

import java.time.LocalDate;

public class GoalStats {

    private Boolean tienePrimerRegistro;
    private LocalDate fechaFin;

    public GoalStats(LocalDate fechaFin) {
        tienePrimerRegistro = false;
        this.fechaFin = fechaFin;
    }

    public GoalStats(Boolean tienePrimerRegistro, LocalDate fechaFin) {
        this.tienePrimerRegistro = tienePrimerRegistro;
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getTienePrimerRegistro() {
        return tienePrimerRegistro;
    }

    public void setTienePrimerRegistro(Boolean tienePrimerRegistro) {
        this.tienePrimerRegistro = tienePrimerRegistro;
    }
}