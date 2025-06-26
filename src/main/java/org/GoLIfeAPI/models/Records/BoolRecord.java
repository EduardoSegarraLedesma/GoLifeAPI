package org.GoLIfeAPI.models.Records;

import jakarta.validation.constraints.NotNull;
import org.bson.Document;

import java.time.LocalDate;

public class BoolRecord extends Record {

    @NotNull(message = "El check no puede estar vacio")
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
        return new Document("fecha", fecha.format(formatter))
                .append("valorBool", valorBool);
    }

    public boolean isValorBool() {
        return valorBool;
    }

    public void setValorBool(boolean valorBool) {
        this.valorBool = valorBool;
    }

}