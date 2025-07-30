package org.GoLifeAPI.dto.goal;

import org.GoLifeAPI.model.Enums;
import org.bson.types.ObjectId;

public class ResponsePartialGoalDTO {

    protected String _id;
    protected String nombre;
    protected Enums.Tipo tipo;
    protected String fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Enums.Duracion duracionUnidad;

    public ResponsePartialGoalDTO(ObjectId _id, String nombre, Enums.Tipo tipo,
                                  String fecha, Boolean finalizado, int duracionValor, Enums.Duracion duracionUnidad) {
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

    public Enums.Tipo getTipo() {
        return tipo;
    }

    public Enums.Duracion getDuracionUnidad() {
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