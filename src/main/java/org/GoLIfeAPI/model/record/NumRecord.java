package org.GoLIfeAPI.model.record;

import org.bson.Document;

import java.time.LocalDate;

public class NumRecord extends Record {

    private Double valorNum;

    public NumRecord() {
        super();
    }

    public NumRecord(Double valorNum, LocalDate fecha) {
        super(fecha);
        this.valorNum = valorNum;
    }

    @Override
    public Document toDocument() {
        return new Document("fecha", fecha.format(formatter))
                .append("valorNum", valorNum);
    }

    public Double getValorNum() {
        return valorNum;
    }

    public void setValorNum(Double valorNum) {
        this.valorNum = valorNum;
    }
}