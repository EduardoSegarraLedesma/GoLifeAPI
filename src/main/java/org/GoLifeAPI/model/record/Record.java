package org.GoLifeAPI.model.record;

import java.time.LocalDate;

public abstract class Record {

    protected LocalDate fecha;

    public Record() {
    }

    public Record(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}