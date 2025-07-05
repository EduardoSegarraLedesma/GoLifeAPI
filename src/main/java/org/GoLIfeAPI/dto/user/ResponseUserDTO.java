package org.GoLIfeAPI.dto.user;

import org.GoLIfeAPI.dto.goal.PartialGoalDTO;
import org.GoLIfeAPI.model.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public class ResponseUserDTO {

    private String nombre;
    private String apellidos;
    private List<PartialGoalDTO> metas;

    public ResponseUserDTO() {
    }

    public ResponseUserDTO(String apellidos, String nombre, List<PartialGoalDTO> metas) {
        this.apellidos = apellidos;
        this.nombre = nombre;
        this.metas = metas;
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

    public List<PartialGoalDTO> getMetas() {
        return metas;
    }

    public void setMetas(List<PartialGoalDTO> metas) {
        this.metas = metas;
    }

    public void addMeta(PartialGoalDTO meta) {
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
