package org.GoLifeAPI.persistence.interfaces;

import org.GoLifeAPI.model.goal.BoolGoal;
import org.GoLifeAPI.model.goal.Goal;
import org.GoLifeAPI.model.goal.NumGoal;
import org.GoLifeAPI.model.user.User;
import org.GoLifeAPI.model.user.UserStats;
import org.bson.Document;

public interface IGoalPersistenceController {

    User createBoolGoal(BoolGoal goal, Document userStatsUpdate, String uid);

    User createNumGoal(NumGoal goal, Document userStatsUpdate, String uid);

    Goal read(String id);

    User updateWithUserStats(Document goalUpdate, Document partialGoalUpdate,
                             Document userStatsUpdate, String uid, String mid);

    User updateWithGoalStats(Document goalUpdate, Document partialGoalUpdate,
                             Document goalStatsUpdate, String uid, String mid);

    UserStats delete(Document userStatsUpdate, String uid, String mid);
}