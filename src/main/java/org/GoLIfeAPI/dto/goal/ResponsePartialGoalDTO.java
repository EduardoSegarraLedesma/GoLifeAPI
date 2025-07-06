package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.Goal;
import org.bson.types.ObjectId;

public class ResponsePartialGoalDTO {

    protected String _id;
    protected String nombre;
    protected Goal.Tipo tipo;
    protected String fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Goal.Duracion duracionUnidad;

    public ResponsePartialGoalDTO(ObjectId _id, String nombre, Goal.Tipo tipo,
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

    public Goal.Tipo getTipo() {
        return tipo;
    }

    public Goal.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public int getDuracionValor() {
        return duracionValor;
    }

    public Boolean getFinalizado() {
        return finalizado;
    }

    public String getFecha() {
        return this.fecha;
    }

    public String getNombre() {
        return nombre;
    }
}