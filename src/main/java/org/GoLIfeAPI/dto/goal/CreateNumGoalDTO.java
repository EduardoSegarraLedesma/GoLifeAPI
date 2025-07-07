package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.*;
import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.goal.NumGoal;

import java.time.LocalDate;

public class CreateNumGoalDTO extends CreateGoalDTO {

    @NotNull(message = "El valor objetivo es obligatorio")
    @Positive(message = "El valor objetivo debe ser un número positivo")
    @Digits(integer = 13, fraction = 2, message = "Formato inválido: máximo 13 cifras enteras y 2 decimales")
    private Double valorObjetivo;
    @NotBlank(message = "La unidad es obligatoria")
    @Size(max = 20, message = "La unidad no debe tener más de 20 caracteres")
    private String unidad;

    public CreateNumGoalDTO() {
        super();
        this.tipo = Enums.Tipo.Num;
    }

    public CreateNumGoalDTO(String nombre, String descripcion, LocalDate fecha, int duracionValor, Enums.Duracion duracionUnidad, Double valorObjetivo, String unidad) {
        super(nombre, Enums.Tipo.Num, descripcion, fecha, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
    }

    public NumGoal toEntity(String uid, LocalDate fechaFin) {
        return new NumGoal(uid, getNombre(), getDescripcion(), getFecha(), false,
                getDuracionValor(), getDuracionUnidad(), false, fechaFin, getValorObjetivo(), getUnidad());
    }

    public @NotNull(message = "El valor objetivo es obligatorio") @Positive(message = "El valor objetivo debe ser un número positivo") @Digits(integer = 13, fraction = 2, message = "Formato inválido: máximo 13 cifras enteras y 2 decimales") Double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(@NotNull(message = "El valor objetivo es obligatorio") @Positive(message = "El valor objetivo debe ser un número positivo") @Digits(integer = 13, fraction = 2, message = "Formato inválido: máximo 13 cifras enteras y 2 decimales") Double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}
