package org.GoLIfeAPI.dto.record;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class ResponseRecordDTO {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    protected LocalDate fecha;

    public ResponseRecordDTO(String fecha) {
        this.fecha = LocalDate.parse(fecha, formatter);
    }

    public LocalDate getFecha() {
        return fecha;
    }
}