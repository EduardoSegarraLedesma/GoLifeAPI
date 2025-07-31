package org.GoLifeAPI.e2e;

import org.GoLifeAPI.util.MongoContainer;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("junit-jupiter")
@SelectClasses({
        UserTestIT.class,
        GoalTestIT.class,
        RecordTestIT.class,
})
public class ApplicationTestSuiteIT extends MongoContainer {
}