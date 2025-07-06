package org.GoLIfeAPI.model.user;

import org.GoLIfeAPI.model.goal.Goal;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private String uid;
    private String nombre;
    private String apellidos;
    private List<Goal> metas;
    private UserStats estadisticas;

    public User() {
        estadisticas = new UserStats();
    }

    public User(String uid, String apellidos, String nombre) {
        this.uid = uid;
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.estadisticas = new UserStats();
    }

    public User(String uid, String apellidos, String nombre, int totalMetas, int totalMetasFinalizadas) {
        this.uid = uid;
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.estadisticas = new UserStats(totalMetas, totalMetasFinalizadas);
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("uid", uid);
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
        doc.append("estadisticas", estadisticas.toDocument());
        return doc;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public UserStats getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(UserStats estadisticas) {
        this.estadisticas = estadisticas;
    }
}