package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;

import java.time.LocalDate;

public class CreateNumGoalDTO extends CreateGoalDTO {

    @NotNull(message = "El valor objetivo es obligatorio")
    @Positive(message = "El valor objetivo debe ser un número positivo")
    private Float valorObjetivo;
    @NotBlank(message = "La unidad es obligatoria")
    @Size(max = 20, message = "La unidad no debe tener más de 20 caracteres")
    private String unidad;

    public CreateNumGoalDTO() {
        super();
        this.tipo = Goal.Tipo.Num;
    }

    public CreateNumGoalDTO(String nombre, String descripcion, LocalDate fecha, int duracionValor, Goal.Duracion duracionUnidad, Float valorObjetivo, String unidad) {
        super(nombre, Goal.Tipo.Num, descripcion, fecha, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
    }

    public NumGoal toEntity(String uid) {
        return new NumGoal(uid, getNombre(), getDescripcion(), getFecha(), false,
                getDuracionValor(), getDuracionUnidad(), getValorObjetivo(), getUnidad());
    }

    public Float getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Float valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
