package org.GoLifeAPI.dto.goal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.GoLifeAPI.model.Enums;

public abstract class PatchGoalDTO {

    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    @Min(value = 1, message = "La duración no puede ser negativa ni cero")
    @Max(value = 10000, message = "La duración es demasiado grande, maximo 10000")
    protected Integer duracionValor;
    protected Enums.Duracion duracionUnidad;

    public PatchGoalDTO() {
        nombre = null;
        descripcion = null;
        duracionValor = null;
        duracionUnidad = null;
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

    public Integer getDuracionValor() {
        return duracionValor;
    }

    public void setDuracionValor(Integer duracionValor) {
        this.duracionValor = duracionValor;
    }

    public Enums.Duracion getDuracionUnidad() {
        return duracionUnidad;
    }

    public void setDuracionUnidad(Enums.Duracion duracionUnidad) {
        this.duracionUnidad = duracionUnidad;
    }
}