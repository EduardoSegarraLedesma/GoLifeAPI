package org.GoLIfeAPI.models.Goals;

import org.bson.Document;

import java.time.LocalDate;

public class BoolGoal extends Goal {

    public BoolGoal() {
        super();
    }

    public BoolGoal(String uid, String nombre, String descripcion, LocalDate fecha, int duracionValor, Duracion duracionUnidad) {
        super(uid, nombre, Tipo.Bool, descripcion, fecha, duracionValor, duracionUnidad);
    }

    @Override
    public Document toDocument() {
        return super.toDocument();
    }
}