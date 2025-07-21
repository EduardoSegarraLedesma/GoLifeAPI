package org.GoLifeAPI.service;

import org.GoLIfeAPI.dto.goal.*;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.mapper.service.GoalDtoMapper;
import org.GoLIfeAPI.mapper.service.GoalPatchMapper;
import org.GoLIfeAPI.mapper.service.RecordDtoMapper;
import org.GoLIfeAPI.mapper.service.UserDtoMapper;
import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.persistence.interfaces.IRecordPersistenceController;
import org.GoLIfeAPI.service.implementation.GoalService;
import org.GoLIfeAPI.service.implementation.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.clearInvocations;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Spy
    private GoalDtoMapper goalDtoMapper = new GoalDtoMapper(new RecordDtoMapper());
    @Spy
    private UserDtoMapper userDtoMapper = new UserDtoMapper(goalDtoMapper);
    @Spy
    private GoalPatchMapper goalPatchMapper = new GoalPatchMapper();
    @Spy
    private StatsService statsService = new StatsService();

    @Mock
    private IRecordPersistenceController recordPersistenceController;

    @InjectMocks
    private GoalService goalService;

    private String uid;
    private String mid;
    private CreateBoolGoalDTO createBoolGoalDTO;
    private CreateNumGoalDTO createNumGoalDTO;
    private PatchBoolGoalDTO patchBoolGoalDTO;
    private PatchNumGoalDTO patchNumGoalDTO;
    private BoolGoal boolGoal;
    private NumGoal numGoal;
    private ResponseUserDTO responseUserDTO;
    private ResponseBoolGoalDTO responseBoolGoalDTO;
    private ResponseNumGoalDTO responseNumGoalDTO;
    private ResponseUserStatsDTO responseUserStatsDTO;


    @BeforeEach
    void setUp() {
        clearInvocations(recordPersistenceController);
        uid = "test-uid";
        mid = "test-mid";
        createBoolGoalDTO = null;
        createNumGoalDTO = null;
        patchBoolGoalDTO = null;
        patchNumGoalDTO = null;
        boolGoal = null;
        numGoal = null;
        responseUserDTO = null;
        responseBoolGoalDTO = null;
        responseNumGoalDTO = null;
        responseUserStatsDTO = null;
    }

    @Nested
    @DisplayName("createBoolGoal")
    class CreateBoolGoal {

    }

    @Nested
    @DisplayName("createNumGoal")
    class CreateNumGoal {

    }

    @Nested
    @DisplayName("getGoal")
    class GetGoal {

    }

    @Nested
    @DisplayName("finalizeGoal")
    class finalizeGoal {

    }

    @Nested
    @DisplayName("updateBoolGoal")
    class UpdateBoolGoal {

    }

    @Nested
    @DisplayName("updateNumGoal")
    class UpdateNumGoal {

    }

    @Nested
    @DisplayName("deleteGoal")
    class DeleteGoal {

    }

    @Nested
    @DisplayName("validateAndGetGoal")
    class ValidateAndGetGoal {

    }
}
