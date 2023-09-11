package liquibase.util

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ShowSummaryUtilCommandTest extends Specification {
    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should show summary output when run multiple times"() {
        given:
        String outputFile = "target/test-classes/output.txt"

        when:
        new File(outputFile).delete()
        CommandUtil.runUpdate(postgres,'changelogs/pgsql/update/showSummaryWithLabels.xml', "testtable1", "none", outputFile)

        then:
        new File(outputFile).getText("UTF-8").contains("Run:                          2")
        new File(outputFile).getText("UTF-8").contains("Filtered out:                 4")

        when:
        new File(outputFile).delete()
        CommandUtil.runUpdate(postgres,'changelogs/pgsql/update/showSummaryWithLabels.xml', "testtable1", "none", outputFile)

        then:
        new File(outputFile).getText("UTF-8").contains("Run:                          0")
        new File(outputFile).getText("UTF-8").contains("Filtered out:                 4")

        cleanup:
        CommandUtil.runDropAll(postgres)
        if (postgres.getConnection() != null) {
            postgres.getConnection().close()
        }
    }
}
