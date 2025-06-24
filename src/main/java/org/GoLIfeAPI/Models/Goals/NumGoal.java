package org.GoLIfeAPI.Models.Goals;

import org.bson.Document;

import java.time.LocalDate;

public class NumGoal extends Goal {

    private Float valorObjetivo;
    private String unidad;

    public NumGoal() {
        super();
        this.tipo = Tipo.Num;
    }

    public NumGoal(String uid, String nombre, String descripcion, LocalDate fecha, Boolean finalizado, int duracionValor, Duracion duracionUnidad, Float valorObjetivo, String unidad) {
        super(uid, nombre, Tipo.Num, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
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

    public Float getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Float valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

}