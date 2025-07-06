package org.GoLIfeAPI.model.record;

import org.bson.Document;

import java.time.LocalDate;

public class NumRecord extends Record {

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