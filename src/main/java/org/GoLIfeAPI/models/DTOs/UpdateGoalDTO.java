package org.GoLIfeAPI.models.DTOs;

import jakarta.validation.constraints.*;
import org.GoLIfeAPI.models.Goals.Goal;
import org.bson.Document;

public abstract class UpdateGoalDTO {

    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    protected Boolean finalizado;
    @Min(value = 0, message = "La duración no puede ser negativa")
    @Max(value = 10000, message = "La duración es demasiado grande, maximo 10000")
    protected int duracionValor;
    protected Goal.Duracion duracionUnidad;

    public UpdateGoalDTO() {
        duracionValor = -1;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (nombre != null && !nombre.isBlank()) doc.append("nombre", nombre);
        if (descripcion != null && !descripcion.isBlank()) doc.append("descripcion", descripcion);
        if (finalizado != null) doc.append("finalizado", finalizado);
        if (duracionValor >= 0) doc.append("duracionValor", duracionValor);
        if (duracionUnidad != null) doc.append("duracionUnidad", duracionUnidad);
        return doc;
    }


    public Document toParcialDocument() {
        Document doc = new Document();
        if (nombre != null && !nombre.isBlank()) doc.append("nombre", nombre);
        if (finalizado != null) doc.append("finalizado", finalizado);
        if (duracionValor >= 1) doc.append("duracionValor", duracionValor);
        if (duracionUnidad != null) doc.append("duracionUnidad", duracionUnidad);
        return doc;
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

    public Goal.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Goal.Duracion duracionUnidad) {
        this.duracionUnidad = duracionUnidad;
    }
}