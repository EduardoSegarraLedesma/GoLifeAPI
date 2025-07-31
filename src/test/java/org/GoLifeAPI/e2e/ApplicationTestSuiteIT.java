package org.GoLifeAPI.e2e;

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
public class ApplicationTestSuiteIT {
}