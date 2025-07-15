package org.GoLIfeAPI.model.goal;

import org.GoLIfeAPI.model.Enums;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class PartialGoal {

    private ObjectId _id;
    private String nombre;
    private Enums.Tipo tipo;
    private LocalDate fecha;
    private Boolean finalizado;
    private int duracionValor;
    private Enums.Duracion duracionUnidad;

    public PartialGoal(ObjectId _id, String nombre, Enums.Tipo tipo, LocalDate fecha,
                       Boolean finalizado, int duracionValor, Enums.Duracion duracionUnidad) {
        this._id = _id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Enums.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Enums.Tipo tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(Boolean finalizado) {
        this.finalizado = finalizado;
    }

    public int getDuracionValor() {
        return duracionValor;
    }

    public void setDuracionValor(int duracionValor) {
        this.duracionValor = duracionValor;
    }

    public Enums.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Enums.Duracion duracionUnidad) {
        this.duracionUnidad = duracionUnidad;
    }

}
