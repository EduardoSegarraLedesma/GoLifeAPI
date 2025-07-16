package org.GoLIfeAPI.model.goal;

import org.GoLIfeAPI.model.Enums;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BoolGoal extends Goal {

    protected List<BoolRecord> registros;

    public BoolGoal(LocalDate fechaFin) {
        super(fechaFin);
    }

    public BoolGoal(String uid, String nombre, String descripcion,
                    LocalDate fecha, Boolean finalizado, int duracionValor,
                    Enums.Duracion duracionUnidad, Boolean valorAlcanzado, LocalDate fechaFin) {
        super(uid, nombre, Enums.Tipo.Bool, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, valorAlcanzado, fechaFin);
    }

    public BoolGoal(String uid, ObjectId _id, String nombre, String descripcion,
                    LocalDate fecha, Boolean finalizado, int duracionValor,
                    Enums.Duracion duracionUnidad, GoalStats estadisticas,
                    List<BoolRecord> registros) {
        super(uid, _id, nombre, Enums.Tipo.Bool, descripcion, fecha,
                finalizado, duracionValor, duracionUnidad, estadisticas);
        this.registros = registros;
    }

    public List<BoolRecord> getRegistros() {
        return registros;
    }

    public void setRegistros(List<BoolRecord> registros) {
        this.registros = registros;
    }

    public void addRegistro(BoolRecord registro) {
        if (registros == null) {
            registros = new ArrayList<>();
        }
        registros.add(registro);
    }

    public boolean removeRegistro(BoolRecord registro) {
        if (registros != null) {
            return registros.remove(registro);
        }
        return false;
    }
}