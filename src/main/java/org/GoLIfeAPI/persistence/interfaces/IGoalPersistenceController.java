package org.GoLIfeAPI.persistence.interfaces;

import org.GoLIfeAPI.model.goal.BoolGoal;
import org.GoLIfeAPI.model.goal.Goal;
import org.GoLIfeAPI.model.goal.NumGoal;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.bson.Document;

public interface IGoalPersistenceController {

    User createBoolGoal(BoolGoal goal, Document userStatsUpdate, String uid);

    User createNumGoal(NumGoal goal, Document userStatsUpdate, String uid);

    Goal readGoal(String id);

    User updateWithUserStats(Document goalUpdate, Document partialGoalUpdate,
                             Document userStatsUpdate, String uid, String mid);

    User updateWithGoalStats(Document goalUpdate, Document partialGoalUpdate,
                             Document goalStatsUpdate, String uid, String mid);

    UserStats delete(Document userStatsUpdate, String uid, String mid);
}