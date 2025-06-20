package org.GoLIfeAPI.Models.Records;

import org.bson.Document;
import org.bson.types.ObjectId;

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
        return new Document("valorNum", valorNum)
                .append("fecha", fecha.toString());
    }

    public Float getValorNum() {
        return valorNum;
    }

    public void setValorNum(Float valorNum) {
        this.valorNum = valorNum;
    }

}