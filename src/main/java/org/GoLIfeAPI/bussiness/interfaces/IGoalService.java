package org.GoLIfeAPI.bussiness.interfaces;

import org.GoLIfeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLIfeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;

public interface IGoalService {


    ResponseUserDTO createBoolGoal(CreateBoolGoalDTO dto, String uid);

    ResponseUserDTO createNumGoal(CreateNumGoalDTO dto, String uid);

    Object getGoal(String uid, String mid);

    ResponseUserDTO finalizeGoal(String uid, String mid);

    ResponseUserDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid);

    ResponseUserDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid);

    ResponseUserStatsDTO deleteGoal(String uid, String mid);
}