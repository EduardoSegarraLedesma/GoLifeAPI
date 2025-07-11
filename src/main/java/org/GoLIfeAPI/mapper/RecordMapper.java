package org.GoLIfeAPI.mapper;

import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLIfeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class RecordMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // Map Input DTOs to POJOs

    public BoolRecord mapCreateBoolRecordDtoToBoolRecord(CreateBoolRecordDTO newRecordDto) {
        return new BoolRecord(newRecordDto.isValorBool(), newRecordDto.getFecha());
    }

    public NumRecord mapCreateNumRecordDtoToBoolRecord(CreateNumRecordDTO newRecordDto) {
        return new NumRecord(newRecordDto.getValorNum(), newRecordDto.getFecha());
    }

    // Map POJOs to Docs

    public Document mapBoolRecordToBoolRecordDoc(BoolRecord boolRecord) {
        return new Document("fecha", boolRecord.getFecha().format(formatter))
                .append("valorBool", boolRecord.isValorBool());
    }

    public Document mapNumRecordToNumRecordDoc(NumRecord numRecord) {
        return new Document("fecha", numRecord.getFecha().format(formatter))
                .append("valorNum", numRecord.getValorNum());
    }

    // Map Docs to Output DTOs

    public ResponseBoolRecordDTO mapBoolRecordDocToResponseBoolRecordDto(Document boolRecordDoc) {
        return new ResponseBoolRecordDTO(
                boolRecordDoc.getBoolean("valorBool"),
                boolRecordDoc.getString("fecha"));
    }

    public ResponseNumRecordDTO mapNumRecordDocToResponseNumRecordDto(Document numRecordDoc) {
        return new ResponseNumRecordDTO(
                numRecordDoc.getDouble("valorNum"),
                numRecordDoc.getString("fecha"));
    }
}