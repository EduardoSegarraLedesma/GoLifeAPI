package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.bson.types.ObjectId;

import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class ResponseGoalDTO {

    protected String _id;
    protected String nombre;
    protected Goal.Tipo tipo;
    protected String descripcion;
    protected String fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Goal.Duracion duracionUnidad;

    public ResponseGoalDTO() {

    }

    public ResponseGoalDTO(String _id, String nombre, Goal.Tipo tipo,
                           String descripcion, String fecha,Boolean finalizado,
                           int duracionValor, Goal.Duracion duracionUnidad) {
        this._id = _id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
        return fecha;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}