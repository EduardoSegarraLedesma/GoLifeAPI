package org.GoLIfeAPI.model.goal;

import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GoalStats {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

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

    public Document toDocument() {
        return new Document()
                .append("valorAlcanzado", valorAlcanzado)
                .append("fechaFin", fechaFin != null ? fechaFin.format(formatter) : "");
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