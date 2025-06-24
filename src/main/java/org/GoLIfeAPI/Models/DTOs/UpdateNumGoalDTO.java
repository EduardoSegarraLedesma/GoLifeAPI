package org.GoLIfeAPI.Models.DTOs;

import org.bson.Document;

public class UpdateNumGoalDTO extends UpdateGoalDTO {

    private Float valorObjetivo;
    private String unidad;

    public UpdateNumGoalDTO() {
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