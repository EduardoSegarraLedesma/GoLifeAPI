package org.GoLIfeAPI.bussiness.interfaces;

import org.GoLIfeAPI.dto.goal.ResponseBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.ResponseNumGoalDTO;
import org.GoLIfeAPI.dto.record.CreateBoolRecordDTO;
import org.GoLIfeAPI.dto.record.CreateNumRecordDTO;

import java.time.LocalDate;

public interface IRecordService {

    ResponseBoolGoalDTO createBoolRecord(CreateBoolRecordDTO dto, String uid, String mid);

    ResponseNumGoalDTO createNumRecord(CreateNumRecordDTO dto, String uid, String mid);

    void deleteRecord(String uid, String mid, LocalDate date);
}