package org.GoLIfeAPI.dto.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.GoLIfeAPI.model.record.NumRecord;

import java.time.LocalDate;

public class CreateNumRecordDTO extends CreateRecordDTO {

    @NotNull(message = "El valor del registro no puede ser nulo")
    @PositiveOrZero(message = "El valor del registro debe ser cero o positivo")
    @Digits(integer = 13, fraction = 2, message = "Formato inválido: máximo 13 cifras enteras y 2 decimales")
    private Double valorNum;

    public CreateNumRecordDTO() {
        super();
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CreateNumRecordDTO(@JsonProperty(value = "valorNum", required = true) Double valorNum,
                              LocalDate fecha) {
        super(fecha);
        this.valorNum = valorNum;
    }

    public NumRecord toEntity() {
        return new NumRecord(valorNum, fecha);
    }

    public Double getValorNum() {
        return valorNum;
    }

    public void setValorNum(Double valorNum) {
        this.valorNum = valorNum;
    }
}
