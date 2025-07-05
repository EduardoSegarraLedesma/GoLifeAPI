package org.GoLIfeAPI.dto.goal;

import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;

import java.time.LocalDate;

public class CreateBoolGoalDTO extends CreateGoalDTO {

    public CreateBoolGoalDTO() {
        super();
    }

    public CreateBoolGoalDTO(String nombre, String descripcion, LocalDate fecha, int duracionValor, Goal.Duracion duracionUnidad) {
        super(nombre, Goal.Tipo.Bool, descripcion, fecha, duracionValor, duracionUnidad);
    }

    public BoolGoal toEntity(String uid) {
        return new BoolGoal(uid, getNombre(), getDescripcion(), getFecha(),false,
                getDuracionValor(), getDuracionUnidad());
    }
}
