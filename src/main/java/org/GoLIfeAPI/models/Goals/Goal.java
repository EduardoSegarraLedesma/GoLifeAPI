package org.GoLIfeAPI.models.Goals;

import jakarta.validation.constraints.*;
import org.GoLIfeAPI.models.Records.Record;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Goal {

    public enum Tipo {
        Num, Bool
    }

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public enum Duracion {
        Dias, Semanas, Meses, Años, Indefinido
    }

    protected ObjectId _id;
    protected String uid;
    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    protected String nombre;
    @NotNull(message = "El tipo de meta es obligatorio")
    protected Tipo tipo;
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    protected String descripcion;
    @NotNull(message = "La fecha de inicio es obligatoria")
    protected LocalDate fecha;
    protected Boolean finalizado;
    @Min(value = 0, message = "La duración no puede ser negativa")
    @Max(value = 10000, message = "La duración es demasiado grande")
    protected int duracionValor;
    @NotNull(message = "La unidad de duración es obligatoria")
    protected Duracion duracionUnidad;
    protected List<Record> registros;

    public Goal() {
        this.tipo = Tipo.Bool;
        finalizado = false;
    }

    public Goal(String uid, String nombre, Tipo tipo, String descripcion, LocalDate fecha, Boolean finalizado, int duracionValor, Duracion duracionUnidad) {
        this.uid = uid;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.finalizado = finalizado;
        this.duracionValor = duracionValor;
        this.duracionUnidad = duracionUnidad;
    }

    public Document toDocument() {
        return new Document()
                .append("uid", uid)
                .append("nombre", nombre)
                .append("tipo", tipo)
                .append("descripcion", descripcion)
                .append("fecha", fecha.format(formatter))
                .append("finalizado", finalizado)
                .append("duracionValor", duracionValor)
                .append("duracionUnidad", duracionUnidad)
                .append("registros", getListaRegistros());
    }

    public Document toParcialDocument() {
        return new Document()
                .append("_id", _id)
                .append("nombre", nombre)
                .append("tipo", tipo)
                .append("finalizado", finalizado)
                .append("duracionValor", duracionValor)
                .append("duracionUnidad", duracionUnidad);
    }

    private List<Document> getListaRegistros() {
        if (registros != null) {
            List<Document> registrosDocs = registros.stream()
                    .map(Record::toDocument)
                    .collect(Collectors.toList());
            return registrosDocs;
        } else {
            return List.of();
        }
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

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

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

    public List<Record> getRegistros() {
        return registros;
    }

    public void setRegistros(List<Record> registros) {
        this.registros = registros;
    }

    public void addRegistro(Record registro) {
        if (registros == null) {
            registros = new ArrayList<>();
        }
        registros.add(registro);
    }

    public boolean removeRegistro(Record registro) {
        if (registros != null) {
            return registros.remove(registro);
        }
        return false;
    }
}