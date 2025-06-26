package org.GoLIfeAPI.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.GoLIfeAPI.models.Goals.Goal;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private String id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    @Size(min = 0, max = 50, message = "Los apellidos deben tener entre 0 y 50 caracteres")
    private String apellidos;
    private List<Goal> metas;

    public User() {
    }

    public User(String id, String apellidos, String nombre) {
        this.id = id;
        this.apellidos = apellidos;
        this.nombre = nombre;
    }

    public Document toDocument(){
        Document doc = new Document();
        doc.append("id", id);
        doc.append("nombre", nombre);
        doc.append("apellidos", apellidos);
        if (metas != null) {
            List<Document> metasDocs = metas.stream()
                    .map(Goal::toParcialDocument)
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

    public void addMeta(Goal meta) {
        if (metas == null) {
            metas = new ArrayList<>();
        }
        metas.add(meta);
    }

    public boolean removeMeta(Goal meta) {
        if (metas != null) {
            return metas.remove(meta);
        }
        return false;
    }
}