package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.GoLIfeAPI.model.goal.Goal;
import org.bson.Document;

public abstract class PatchGoalDTO {

    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    @Min(value = 0, message = "La duración no puede ser negativa")
    @Max(value = 10000, message = "La duración es demasiado grande, maximo 10000")
    protected Integer duracionValor;
    protected Goal.Duracion duracionUnidad;

    public PatchGoalDTO() {
        duracionValor = -1;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (nombre != null && !nombre.isBlank()) doc.append("nombre", nombre);
        if (descripcion != null && !descripcion.isBlank()) doc.append("descripcion", descripcion);
        if (duracionValor >= 0) doc.append("duracionValor", duracionValor);
        if (duracionUnidad != null) doc.append("duracionUnidad", duracionUnidad);
        return doc;
    }


    public Document toParcialDocument() {
        Document doc = new Document();
        if (nombre != null && !nombre.isBlank()) doc.append("nombre", nombre);
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