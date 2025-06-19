package org.GoLIfeAPI.Models.Records;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class BoolRecord {

    private boolean valorBool;
    private LocalDate fecha;

    public BoolRecord() {
    }

    public BoolRecord(boolean valorBool, LocalDate fecha) {
        this.valorBool = valorBool;
        this.fecha = fecha;
    }

    public Document toDocument() {
        return new Document("_id", new ObjectId()) // ID autogenerado
                .append("valorBool", valorBool)
                .append("fecha", fecha.toString()); // formato "yyyy-MM-dd"
    }

    public boolean isValorBool() {
        return valorBool;
    }

    public void setValorBool(boolean valorBool) {
        this.valorBool = valorBool;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

}