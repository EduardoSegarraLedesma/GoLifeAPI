package org.GoLifeAPI.mixed.persistence;

import org.GoLifeAPI.util.MongoContainer;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.testcontainers.junit.jupiter.Testcontainers;

@Suite
@IncludeEngines("junit-jupiter")
@SelectClasses({
        UserPersistenceControllerTest.class,
        GoalPersistenceControllerTest.class,
        RecordPersistenceControllerTest.class,
})
@Testcontainers
public class PersistenceControllerSuiteTest extends MongoContainer {


}