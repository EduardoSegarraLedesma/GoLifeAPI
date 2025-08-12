package org.GoLifeAPI.model.goal;

import org.GoLifeAPI.model.Enums;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class PartialNumGoal extends PartialGoal {

    private Double valorObjetivo;
    private String unidad;

    public PartialNumGoal(ObjectId _id, String nombre, Enums.Tipo tipo, LocalDate fecha,
                          Boolean finalizado, int duracionValor, Enums.Duracion duracionUnidad,
                          Double valorObjetivo, String unidad) {

        super(_id, nombre, tipo, fecha, finalizado, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
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