package org.GoLifeAPI.mapper.persistence;

import org.GoLifeAPI.model.record.BoolRecord;
import org.GoLifeAPI.model.record.NumRecord;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RecordDocMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // Map POJOs to DOCs

    public Document mapBoolRecordToDoc(BoolRecord boolRecord) {
        return new Document("fecha", boolRecord.getFecha().format(formatter))
                .append("valorBool", boolRecord.isValorBool());
    }

    public Document mapNumRecordToDoc(NumRecord numRecord) {
        return new Document("fecha", numRecord.getFecha().format(formatter))
                .append("valorNum", numRecord.getValorNum());
    }

    // Map DOCs to POJOs

    public BoolRecord mapDocToBoolRecord(Document boolRecordDoc) {
        return new BoolRecord(
                boolRecordDoc.getBoolean("valorBool"),
                LocalDate.parse(boolRecordDoc.getString("fecha")));
    }

    public NumRecord mapDocToNumRecord(Document numRecordDoc) {
        return new NumRecord(
                numRecordDoc.getDouble("valorNum"),
                LocalDate.parse(numRecordDoc.getString("fecha")));
    }
}