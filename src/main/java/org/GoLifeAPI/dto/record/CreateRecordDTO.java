package org.GoLifeAPI.dto.record;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public abstract class CreateRecordDTO {

    @NotNull(message = "La fecha no puede estar vacia")
    protected LocalDate fecha;

    public CreateRecordDTO() {
    }

    public CreateRecordDTO(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
