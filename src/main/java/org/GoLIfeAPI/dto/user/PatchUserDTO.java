package org.GoLIfeAPI.dto.user;

import jakarta.validation.constraints.Size;
import org.bson.Document;

public class PatchUserDTO {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    @Size(max = 50, message = "Los apellidos deben tener entre 0 y 50 caracteres")
    private String apellidos;

    public PatchUserDTO() {
        apellidos = "";
    }

    public Document toDocument() {
        Document doc = new Document();
        if (nombre != null && !nombre.isBlank()) doc.append("nombre", nombre);
        if (apellidos != null && !apellidos.isBlank()) doc.append("apellidos", apellidos);
        return doc;
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