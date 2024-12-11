package liquibase.integration.ant.test

import liquibase.analytics.TestAnalyticsWebserver
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.ProjectComponent
import org.apache.tools.ant.taskdefs.condition.Condition

/**
 * Check that the post body sent to the temporary analytics endpoint contains liquibaseInterface=ant.
 */
class AnalyticsWebserverPostBodyIsAnt extends ProjectComponent implements Condition{
    @Override
    boolean eval() throws BuildException {
        return TestAnalyticsWebserver.postBody != null && TestAnalyticsWebserver.postBody.contains('liquibaseInterface": "ant')
    }
}
