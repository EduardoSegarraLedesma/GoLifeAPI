package org.GoLIfeAPI.dto.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateBoolRecordDTO extends CreateRecordDTO {
    @NotNull(message = "El check no puede estar vacio")
    private boolean valorBool;

    public CreateBoolRecordDTO() {
        super();
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CreateBoolRecordDTO(@JsonProperty(value = "valorBool", required = true) boolean valorBool,
                               LocalDate fecha) {
        super(fecha);
        this.valorBool = valorBool;
    }

    public boolean isValorBool() {
        return valorBool;
    }

    public void setValorBool(boolean valorBool) {
        this.valorBool = valorBool;
    }
}