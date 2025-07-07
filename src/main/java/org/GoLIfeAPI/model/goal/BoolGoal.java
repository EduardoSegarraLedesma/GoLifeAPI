package org.GoLIfeAPI.model.goal;

import org.GoLIfeAPI.model.Enums;
import org.bson.Document;

import java.time.LocalDate;

public class BoolGoal extends Goal {

    public BoolGoal(LocalDate fechaFin) {
        super(fechaFin);
    }

    public BoolGoal(String uid, String nombre, String descripcion,
                    LocalDate fecha, Boolean finalizado, int duracionValor,
                    Enums.Duracion duracionUnidad, Boolean valorAlcanzado, LocalDate fechaFin) {
        super(uid, nombre, Enums.Tipo.Bool, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, valorAlcanzado, fechaFin);
    }

    @Override
    public Document toDocument() {
        return super.toDocument();
    }
}