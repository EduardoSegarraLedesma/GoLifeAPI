package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.*;
import org.GoLIfeAPI.model.goal.Goal;

import java.time.LocalDate;

public abstract class CreateGoalDTO {

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    protected Goal.Tipo tipo;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    @NotNull(message = "La fecha de inicio es obligatoria")
    protected LocalDate fecha;
    @Min(value = 0, message = "La duración no puede ser negativa")
    @Max(value = 10000, message = "La duración es demasiado grande, maximo 10000")
    protected int duracionValor;
    @NotNull(message = "La unidad de duración es obligatoria")
    protected Goal.Duracion duracionUnidad;


    public CreateGoalDTO() {
        this.tipo = Goal.Tipo.Bool;
    }

    public CreateGoalDTO(String nombre, Goal.Tipo tipo, String descripcion, LocalDate fecha, int duracionValor, Goal.Duracion duracionUnidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
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