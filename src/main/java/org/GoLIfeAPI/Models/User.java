package org.GoLIfeAPI.Models;

import org.GoLIfeAPI.Models.Goals.Goal;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private String id;
    private String nombre;
    private String apellidos;
    private List<Goal> metas;

    public User() {
        metas = new ArrayList<>();
    }

    public User(String id, String apellidos, String nombre) {
        this.id = id;
        this.apellidos = apellidos;
        this.nombre = nombre;
        metas = new ArrayList<>();
    }

    public Document toDocument(){
        Document doc = new Document();
        doc.append("id", id);
        doc.append("nombre", nombre);
        doc.append("apellidos", apellidos);
        if (metas != null) {
            List<Document> metasDocs = metas.stream()
                    .map(Goal::toDocument)
                    .collect(Collectors.toList());
            doc.append("metas", metasDocs);
        } else {
            doc.append("metas", List.of());
        }
        return doc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Goal> getMetas() {
        return metas;
    }

    public void setMetas(List<Goal> metas) {
        this.metas = metas;
    }
}