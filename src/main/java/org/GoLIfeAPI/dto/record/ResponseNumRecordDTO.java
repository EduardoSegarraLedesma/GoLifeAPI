package org.GoLIfeAPI.dto.record;

public class ResponseNumRecordDTO extends ResponseRecordDTO {

    private Double valorNum;

    public ResponseNumRecordDTO(Double valorNum, String fecha) {
        super(fecha);
        this.valorNum = valorNum;
    }

    public Double getValorNum() {
        return valorNum;
    }
}
