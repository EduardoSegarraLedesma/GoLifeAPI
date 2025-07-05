package org.GoLIfeAPI.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.bson.Document;

import java.time.LocalDate;

public class BoolRecord extends Record {

    @NotNull(message = "El check no puede estar vacio")
    private boolean valorBool;

    public BoolRecord() {
        super();
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BoolRecord(
            @JsonProperty(value = "valorBool", required = true) boolean valorBool,
            LocalDate fecha) {
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