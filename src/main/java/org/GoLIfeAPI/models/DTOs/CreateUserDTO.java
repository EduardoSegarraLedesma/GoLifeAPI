package org.GoLIfeAPI.models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.GoLIfeAPI.models.User;

public class CreateUserDTO {


    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    @Size(min = 0, max = 50, message = "Los apellidos deben tener entre 0 y 50 caracteres")
    private String apellidos;


    public CreateUserDTO() {
        apellidos = "";
    }

    public User getUserPOJO(String uid){
        return new User(uid, getApellidos(), getNombre());
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