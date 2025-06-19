package org.GoLIfeAPI.Models.Goals;

import org.GoLIfeAPI.Models.Records.BoolRecord;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoolGoal extends Goal {

    private List<BoolRecord> registros;

    public BoolGoal() {
        super();
        registros = new ArrayList<>();
    }

    public BoolGoal(String nombre, String descripcion, LocalDate fecha, int finalizado, int duracionValor, Duracion duracionUnidad) {
        super(nombre, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        registros = new ArrayList<>();
    }

    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        if (registros != null) {
            List<Document> registrosDocs = registros.stream()
                    .map(BoolRecord::toDocument)
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }

        return doc;
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