package org.GoLIfeAPI.model.goal;

import org.GoLIfeAPI.model.Enums;
import org.bson.Document;

import java.time.LocalDate;

public class NumGoal extends Goal {

    private Double valorObjetivo;
    private String unidad;

    public NumGoal(LocalDate fechaFin) {
        super(fechaFin);
        this.tipo = Enums.Tipo.Num;
    }

    public NumGoal(String uid, String nombre, String descripcion,
                   LocalDate fecha, Boolean finalizado, int duracionValor,
                   Enums.Duracion duracionUnidad, Boolean valorAlcanzado, LocalDate fechaFin,
                   Double valorObjetivo, String unidad) {
        super(uid, nombre, Enums.Tipo.Num, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, valorAlcanzado, fechaFin);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
    }

    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("valorObjetivo", valorObjetivo);
        doc.append("unidad", unidad);
        return doc;
    }

    public Double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}