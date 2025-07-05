package org.GoLIfeAPI.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.bson.Document;

public class PatchNumGoalDTO extends PatchGoalDTO {

    @NotNull(message = "El valor objetivo es obligatorio")
    @Positive(message = "El valor objetivo debe ser un número positivo")
    private Float valorObjetivo;
    @NotBlank(message = "La unidad no puede estar vacía")
    @Size(max = 20, message = "La unidad no debe tener más de 20 caracteres")
    private String unidad;

    public PatchNumGoalDTO() {
        super();
    }

    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        if (valorObjetivo != null) doc.append("valorObjetivo", valorObjetivo);
        if (unidad != null || !unidad.isBlank()) doc.append("unidad", unidad);
        return doc;
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