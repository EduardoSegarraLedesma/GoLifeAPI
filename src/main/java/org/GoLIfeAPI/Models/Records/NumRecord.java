package org.GoLIfeAPI.Models.Records;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class NumRecord {

    private Float valorNum;
    private LocalDate fecha;

    public NumRecord() {
    }

    public NumRecord(Float valorNum, LocalDate fecha) {
        this.valorNum = valorNum;
        this.fecha = fecha;
    }

    public Document toDocument() {
        return new Document("_id", new ObjectId())
                .append("valorNum", valorNum)
                .append("fecha", fecha.toString());
    }

    public Float getValorNum() {
        return valorNum;
    }

    public void setValorNum(Float valorNum) {
        this.valorNum = valorNum;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}