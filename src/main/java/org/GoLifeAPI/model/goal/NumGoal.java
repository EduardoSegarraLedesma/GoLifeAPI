package org.GoLifeAPI.model.goal;

import org.GoLifeAPI.model.Enums;
import org.GoLifeAPI.model.record.NumRecord;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NumGoal extends Goal {

    protected List<NumRecord> registros;
    private Double valorObjetivo;
    private String unidad;

    public NumGoal(LocalDate fechaFin) {
        super(fechaFin);
        this.tipo = Enums.Tipo.Num;
    }

    public NumGoal(String uid, String nombre, String descripcion,
                   LocalDate fecha, Boolean finalizado, int duracionValor,
                   Enums.Duracion duracionUnidad, Boolean tienePrimerRegistro, LocalDate fechaFin,
                   Double valorObjetivo, String unidad) {
        super(uid, nombre, Enums.Tipo.Num, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, tienePrimerRegistro, fechaFin);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
    }

    public NumGoal(String uid, ObjectId _id, String nombre, String descripcion,
                   LocalDate fecha, Boolean finalizado, int duracionValor,
                   Enums.Duracion duracionUnidad, GoalStats estadisticas,
                   List<NumRecord> registros, Double valorObjetivo, String unidad) {
        super(uid, _id, nombre, Enums.Tipo.Num, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, estadisticas);
        this.registros = registros;
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
    }

    public List<NumRecord> getRegistros() {
        return registros;
    }

    public void setRegistros(List<NumRecord> registros) {
        this.registros = registros;
    }

    public void addRegistro(NumRecord registro) {
        if (registros == null) {
            registros = new ArrayList<>();
        }
        registros.add(registro);
    }

    public boolean removeRegistro(NumRecord registro) {
        if (registros != null) {
            return registros.remove(registro);
        }
        return false;
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