package org.GoLIfeAPI.persistence.interfaces;

import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.record.BoolRecord;
import org.GoLIfeAPI.model.record.NumRecord;
import org.bson.Document;

public interface IRecordPersistenceController {

    BoolGoal createBoolrecord(BoolRecord record, Document goalStatsUpdate, String mid);

    NumGoal createNumRecord(NumRecord record, Document goalStatsUpdate, String mid);

    void delete(String mid, String date);
}
