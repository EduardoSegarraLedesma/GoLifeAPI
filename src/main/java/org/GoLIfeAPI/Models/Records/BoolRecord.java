package org.GoLIfeAPI.Models.Records;

import org.bson.Document;

import java.time.LocalDate;

public class BoolRecord extends Record {

    private boolean valorBool;

    public BoolRecord() {
        super();
    }

    public BoolRecord(boolean valorBool, LocalDate fecha) {
        super(fecha);
        this.valorBool = valorBool;
    }

    @Override
    public Document toDocument() {
        return new Document("valorBool", valorBool)
                .append("fecha", fecha.toString());
    }

    public boolean isValorBool() {
        return valorBool;
    }

    public void setValorBool(boolean valorBool) {
        this.valorBool = valorBool;
    }

}