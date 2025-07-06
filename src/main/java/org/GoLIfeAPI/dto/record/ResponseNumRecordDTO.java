package org.GoLIfeAPI.dto.record;

public class ResponseNumRecordDTO extends ResponseRecordDTO {

    private Float valorNum;

    public ResponseNumRecordDTO(Float valorNum, String fecha) {
        super(fecha);
        this.valorNum = valorNum;
    }

    public Float getValorNum() {
        return valorNum;
    }
}
