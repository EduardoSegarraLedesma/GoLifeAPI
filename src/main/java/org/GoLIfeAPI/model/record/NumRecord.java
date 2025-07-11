package org.GoLIfeAPI.model.record;

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

    public Double getValorNum() {
        return valorNum;
    }

    public void setValorNum(Double valorNum) {
        this.valorNum = valorNum;
    }
}