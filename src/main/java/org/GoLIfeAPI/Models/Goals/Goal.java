package org.GoLIfeAPI.Models.Goals;

import org.bson.Document;

import java.time.LocalDate;

public abstract class Goal {

    public enum Duracion {
        Dias, Semanas, Meses, Años, Indefinido
    }

    protected String nombre;
    protected String descripcion;
    protected LocalDate fecha;
    protected int finalizado;
    protected int duracionValor;
    protected Duracion duracionUnidad;

    public Goal() {
    }

    public Goal(String nombre, String descripcion, LocalDate fecha, int finalizado, int duracionValor, Duracion duracionUnidad) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
    }

    public Document toDocument() {
        return new Document()
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("fecha", fecha != null ? fecha.toString() : null)
                .append("finalizado", finalizado)
                .append("duracionValor", duracionValor)
                .append("duracionUnidad", duracionUnidad != null ? duracionUnidad.name() : null);
    }

    // Getters y setters

    public Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Duracion duracionUnidad) {
        this.duracionUnidad = duracionUnidad;
    }

    public int getDuracionValor() {
        return duracionValor;
    }

    public void setDuracionValor(int duracionValor) {
        this.duracionValor = duracionValor;
    }

    public int getFinalizado() {
        return finalizado;
    }

    public void setFinalizado(int finalizado) {
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
}
