package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.*;
import org.GoLIfeAPI.model.Enums;

import java.time.LocalDate;

public abstract class CreateGoalDTO {

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    protected Enums.Tipo tipo;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    @NotNull(message = "La fecha de inicio es obligatoria")
    protected LocalDate fecha;
    @Min(value = 1, message = "La duración no puede ser negativa ni cero")
    @Max(value = 10000, message = "La duración es demasiado grande, maximo 10000")
    protected int duracionValor;
    @NotNull(message = "La unidad de duración es obligatoria")
    protected Enums.Duracion duracionUnidad;


    public CreateGoalDTO() {
        this.tipo = Enums.Tipo.Bool;
    }

    public CreateGoalDTO(String nombre, Enums.Tipo tipo, String descripcion, LocalDate fecha, int duracionValor, Enums.Duracion duracionUnidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.duracionUnidad = duracionUnidad;
        this.duracionValor = duracionValor;
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