package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.BoolGoal;

import java.time.LocalDate;

public class CreateBoolGoalDTO extends CreateGoalDTO {

    public CreateBoolGoalDTO() {
        super();
    }

    public CreateBoolGoalDTO(String nombre, String descripcion, LocalDate fecha, int duracionValor, Enums.Duracion duracionUnidad) {
        super(nombre, Enums.Tipo.Bool, descripcion, fecha, duracionValor, duracionUnidad);
    }

    public BoolGoal toEntity(String uid, LocalDate fechaFin) {
        return new BoolGoal(uid, getNombre(), getDescripcion(), getFecha(), false,
                getDuracionValor(), getDuracionUnidad(), false, fechaFin);
    }
}
