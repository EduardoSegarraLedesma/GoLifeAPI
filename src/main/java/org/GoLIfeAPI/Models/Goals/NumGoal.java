package org.GoLIfeAPI.Models.Goals;

import org.GoLIfeAPI.Models.Records.NumRecord;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NumGoal extends Goal {

    private Float valorObjetivo;
    private String unidad;
    private List<NumRecord> registros;

    public NumGoal() {
        super();
        registros = new ArrayList<>();
    }

    public NumGoal(String nombre, String descripcion, LocalDate fecha, int finalizado, int duracionValor, Duracion duracionUnidad, Float valorObjetivo, String unidad) {
        super(nombre, descripcion, fecha, finalizado, duracionValor, duracionUnidad);
        this.valorObjetivo = valorObjetivo;
        this.unidad = unidad;
        registros = new ArrayList<>();
    }

    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("valorObjetivo", valorObjetivo);
        doc.append("unidad", unidad);
        if (registros != null) {
            List<Document> registrosDocs = registros.stream()
                    .map(NumRecord::toDocument)
                    .collect(Collectors.toList());
            doc.append("registros", registrosDocs);
        } else {
            doc.append("registros", List.of());
        }
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

    public List<NumRecord> getRegistros() {
        return registros;
    }

    public void setRegistros(List<NumRecord> registros) {
        this.registros = registros;
    }

    public void addRegistro(NumRecord registro) {
        if (registros == null) {
            registros = new ArrayList<>();
        }
        registros.add(registro);
    }

    public boolean removeRegistro(NumRecord registro) {
        if (registros != null) {
            return registros.remove(registro);
        }
        return false;
    }
}