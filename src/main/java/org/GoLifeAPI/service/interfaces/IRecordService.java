package org.GoLifeAPI.service.interfaces;

import org.GoLifeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLifeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLifeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLifeAPI.dto.record.CreateNumRecordDTO;

import java.time.LocalDate;

public interface IRecordService {

    ResponseBoolGoalDTO createBoolRecord(CreateBoolRecordDTO dto, String uid, String mid);

    ResponseNumGoalDTO createNumRecord(CreateNumRecordDTO dto, String uid, String mid);

    void deleteRecord(String uid, String mid, LocalDate date);
}