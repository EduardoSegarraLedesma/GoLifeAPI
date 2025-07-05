package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.Goal;
import org.bson.types.ObjectId;

public class PartialGoalDTO {

    protected String _id;
    protected String nombre;
    protected Goal.Tipo tipo;
    protected String fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Goal.Duracion duracionUnidad;

    public PartialGoalDTO() {
    }

    public PartialGoalDTO(ObjectId _id, String nombre, Goal.Tipo tipo,
                          String fecha, Boolean finalizado, int duracionValor, Goal.Duracion duracionUnidad) {
        this._id = _id.toString();
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id.toString();
    }

    public Goal.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Goal.Tipo tipo) {
        this.tipo = tipo;
    }

    public Goal.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Goal.Duracion duracionUnidad) {
        this.duracionUnidad = duracionUnidad;
    }

    public int getDuracionValor() {
        return duracionValor;
    }

    public void setDuracionValor(int duracionValor) {
        this.duracionValor = duracionValor;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(Boolean finalizado) {
        this.finalizado = finalizado;
    }

    public String getFecha() {
        return this.fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}