package org.GoLifeAPI.dto.record;

public class ResponseBoolRecordDTO extends ResponseRecordDTO {

    private boolean valorBool;

    public ResponseBoolRecordDTO(boolean valorBool, String fecha) {
        super(fecha);
        this.valorBool = valorBool;
    }

    public boolean isValorBool() {
        return valorBool;
    }
}