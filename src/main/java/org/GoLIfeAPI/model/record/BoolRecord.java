package org.GoLIfeAPI.model.record;

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

    public boolean isValorBool() {
        return valorBool;
    }

    public void setValorBool(boolean valorBool) {
        this.valorBool = valorBool;
    }
}