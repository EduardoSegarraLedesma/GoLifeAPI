package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.Enums;

import java.time.LocalDate;

public class CreateBoolGoalDTO extends CreateGoalDTO {

    public CreateBoolGoalDTO() {
        super();
    }

    public CreateBoolGoalDTO(String nombre, String descripcion, LocalDate fecha, int duracionValor, Enums.Duracion duracionUnidad) {
        super(nombre, Enums.Tipo.Bool, descripcion, fecha, duracionValor, duracionUnidad);
    }
}