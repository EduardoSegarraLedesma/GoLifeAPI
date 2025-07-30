package org.GoLifeAPI.model.goal;

import java.time.LocalDate;

public class GoalStats {

    private Boolean valorAlcanzado;
    private LocalDate fechaFin;

    public GoalStats(LocalDate fechaFin) {
        valorAlcanzado = false;
        this.fechaFin = fechaFin;
    }

    public GoalStats(Boolean valorAlcanzado, LocalDate fechaFin) {
        this.valorAlcanzado = valorAlcanzado;
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getValorAlcanzado() {
        return valorAlcanzado;
    }

    public void setValorAlcanzado(Boolean valorAlcanzado) {
        this.valorAlcanzado = valorAlcanzado;
    }
}