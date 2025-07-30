package org.GoLifeAPI.persistence.interfaces;

import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.model.record.BoolRecord;
import org.GoLifeAPI.model.record.NumRecord;
import org.bson.Document;

public interface IRecordPersistenceController {

    BoolGoal createBoolrecord(BoolRecord record, Document goalStatsUpdate, String mid);

    NumGoal createNumRecord(NumRecord record, Document goalStatsUpdate, String mid);

    void delete(String mid, String date);
}
