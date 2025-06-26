package org.GoLIfeAPI.models.Records;

import jakarta.validation.constraints.*;
import org.bson.Document;

import java.time.LocalDate;

public class NumRecord extends Record {

    @NotNull(message = "El valor del registro no puede ser nulo")
    @PositiveOrZero(message = "El valor del registro debe ser cero o positivo")
    @Digits(integer = 8, fraction = 2, message = "Formato inválido: máximo 8 cifras enteras y 2 decimales")
    private Float valorNum;

    public NumRecord() {
        super();
    }

    public NumRecord(Float valorNum, LocalDate fecha) {
        super(fecha);
        this.valorNum = valorNum;
    }

    @Override
    public Document toDocument() {
        return new Document("fecha", fecha.format(formatter))
                .append("valorNum", valorNum);
    }

    public Float getValorNum() {
        return valorNum;
    }

    public void setValorNum(Float valorNum) {
        this.valorNum = valorNum;
    }

}