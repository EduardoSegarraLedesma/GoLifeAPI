package org.GoLifeAPI.model.user;

import org.GoLifeAPI.model.goal.PartialGoal;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String uid;
    private String nombre;
    private String apellidos;
    private List<PartialGoal> metas;
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

    public User(String uid,  String nombre,String apellidos, List<PartialGoal> metas, UserStats estadisticas) {
        this.uid = uid;
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.metas = metas;
        this.estadisticas = estadisticas;
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

    public List<PartialGoal> getMetas() {
        return metas;
    }

    public void setMetas(List<PartialGoal> metas) {
        this.metas = metas;
    }

    public void addMeta(PartialGoal meta) {
        if (metas == null) {
            metas = new ArrayList<>();
        }
        metas.add(meta);
    }

    public boolean removeMeta(PartialGoal meta) {
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