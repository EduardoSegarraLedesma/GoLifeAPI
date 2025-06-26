package org.GoLIfeAPI.models.Records;

import jakarta.validation.constraints.NotNull;
import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class Record {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    @NotNull(message = "La fecha no puede estar vacia")
    protected LocalDate fecha;

    public Record() {
    }

    public Record(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Document toDocument() {
        return new Document();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}