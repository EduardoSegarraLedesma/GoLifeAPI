package org.GoLifeAPI.service.interfaces;

import org.GoLifeAPI.dto.goal.CreateBoolGoalDTO;
import org.GoLifeAPI.dto.goal.CreateNumGoalDTO;
import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLifeAPI.dto.user.ResponseUserDTO;
import org.GoLifeAPI.dto.user.ResponseUserStatsDTO;

public interface IGoalService {

    ResponseUserDTO createBoolGoal(CreateBoolGoalDTO dto, String uid);

    ResponseUserDTO createNumGoal(CreateNumGoalDTO dto, String uid);

    Object getGoal(String uid, String mid);

    ResponseUserDTO finalizeGoal(String uid, String mid);

    ResponseUserDTO updateBoolGoal(PatchBoolGoalDTO dto, String uid, String mid);

    ResponseUserDTO updateNumGoal(PatchNumGoalDTO dto, String uid, String mid);

    ResponseUserStatsDTO deleteGoal(String uid, String mid);
}