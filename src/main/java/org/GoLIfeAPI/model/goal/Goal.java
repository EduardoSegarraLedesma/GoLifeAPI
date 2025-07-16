package org.GoLIfeAPI.model.goal;

import org.GoLIfeAPI.model.Enums;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public abstract class Goal {

    protected ObjectId _id;
    protected String uid;
    protected String nombre;
    protected Enums.Tipo tipo;
    protected String descripcion;
    protected LocalDate fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Enums.Duracion duracionUnidad;
    protected GoalStats estadisticas;

    public Goal(LocalDate fechaFin) {
        this.tipo = Enums.Tipo.Bool;
        finalizado = false;
        estadisticas = new GoalStats(fechaFin);
    }

    public Goal(String uid, String nombre, Enums.Tipo tipo,
                String descripcion, LocalDate fecha, Boolean finalizado,
                int duracionValor, Enums.Duracion duracionUnidad, Boolean valorAlcanzado, LocalDate fechaFin) {
        this.uid = uid;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionUnidad = duracionUnidad;
        if (this.duracionUnidad.toString().equalsIgnoreCase("Indefinido"))
            this.duracionValor = -1;
        else
            this.duracionValor = duracionValor;
        estadisticas = new GoalStats(valorAlcanzado, fechaFin);
    }

    public Goal(String uid, ObjectId _id, String nombre, Enums.Tipo tipo,
                String descripcion, LocalDate fecha, Boolean finalizado,
                int duracionValor, Enums.Duracion duracionUnidad, GoalStats estadisticas) {
        this.uid = uid;
        this._id = _id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionUnidad = duracionUnidad;
        if (this.duracionUnidad.toString().equalsIgnoreCase("Indefinido"))
            this.duracionValor = -1;
        else
            this.duracionValor = duracionValor;
        this.estadisticas = estadisticas;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Enums.Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Enums.Tipo tipo) {
        this.tipo = tipo;
    }

    public Enums.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Enums.Duracion duracionUnidad) {
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
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

    public GoalStats getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(GoalStats estadisticas) {
        this.estadisticas = estadisticas;
    }
}