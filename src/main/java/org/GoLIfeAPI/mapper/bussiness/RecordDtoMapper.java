package org.GoLIfeAPI.mapper.bussiness;

import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class RecordDtoMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // Map Input DTOs to POJOs

    public BoolRecord mapCreateBoolRecordDtoToBoolRecord(CreateBoolRecordDTO newRecordDto) {
        return new BoolRecord(newRecordDto.isValorBool(), newRecordDto.getFecha());
    }

    public NumRecord mapCreateNumRecordDtoToBoolRecord(CreateNumRecordDTO newRecordDto) {
        return new NumRecord(newRecordDto.getValorNum(), newRecordDto.getFecha());
    }

    // Map POJOs to Output DTOs

    public ResponseBoolRecordDTO mapBoolRecordToResponseBoolRecordDto(BoolRecord boolRecord) {
        return new ResponseBoolRecordDTO(
                boolRecord.isValorBool(),
                boolRecord.getFecha().format(formatter));
    }

    public ResponseNumRecordDTO mapNumRecordToResponseNumRecordDto(NumRecord numRecord) {
        return new ResponseNumRecordDTO(
                numRecord.getValorNum(),
                numRecord.getFecha().format(formatter));
    }
}