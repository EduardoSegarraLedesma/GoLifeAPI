package org.GoLifeAPI.dto.goal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PatchNumGoalDTO extends PatchGoalDTO {

    @Positive(message = "El valor objetivo debe ser un número positivo")
    @Digits(integer = 13, fraction = 2, message = "Formato inválido: máximo 13 cifras enteras y 2 decimales")
    private Double valorObjetivo;
    @Size(max = 20, message = "La unidad no debe tener más de 20 caracteres")
    private String unidad;

    public PatchNumGoalDTO() {
        super();
        valorObjetivo = null;
        unidad = null;
    }

    public Double getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(Double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
}