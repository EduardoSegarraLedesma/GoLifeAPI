package org.GoLIfeAPI.Models.Records;

import org.bson.Document;

import java.time.LocalDate;

public abstract class Record {

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