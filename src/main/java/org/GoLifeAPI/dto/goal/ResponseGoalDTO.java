package org.GoLifeAPI.dto.goal;

import org.GoLifeAPI.model.Enums;

public abstract class ResponseGoalDTO {

    protected String _id;
    protected String nombre;
    protected Enums.Tipo tipo;
    protected String descripcion;
    protected String fecha;
    protected Boolean finalizado;
    protected int duracionValor;
    protected Enums.Duracion duracionUnidad;
    protected ResponseGoalStatsDTO estadisticas;

    public ResponseGoalDTO(String _id, String nombre, Enums.Tipo tipo,
                           String descripcion, String fecha, Boolean finalizado,
                           int duracionValor, Enums.Duracion duracionUnidad, ResponseGoalStatsDTO estadisticas) {
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
        return fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ResponseGoalStatsDTO getEstadisticas() {
        return estadisticas;
    }
}