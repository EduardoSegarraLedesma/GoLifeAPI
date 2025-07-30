package org.GoLifeAPI.dto.user;

import jakarta.validation.constraints.Size;

public class PatchUserDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    @Size(max = 50, message = "Los apellidos deben tener entre 0 y 50 caracteres")
    private String apellidos;

    public PatchUserDTO() {
        apellidos = "";
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
}