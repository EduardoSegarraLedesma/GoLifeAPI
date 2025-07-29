package org.GoLifeAPI.persistence;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("junit-jupiter")
@SelectClasses({
        UserPersistenceControllerTest.class,
        GoalPersistenceControllerTest.class,
        RecordPersistenceControllerTest.class,
})
public class PersistenceControllerSuiteTest extends MongoContainer {

}