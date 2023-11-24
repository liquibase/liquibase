package liquibase.command.core

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class UpdateTestingRollbackCommandsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run UpdateTestingRollback from Liquibase class"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), h2.getDatabaseFromFactory())
        liquibase.updateTestingRollback(new Contexts(), new LabelExpression())

        then:
        def resultSet = h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def rsTableExist = h2.getConnection().createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "run UpdateTestingRollback with changelog parameter from Liquibase class"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests-with-params.yml", new ClassLoaderResourceAccessor(), h2.getDatabaseFromFactory())
        liquibase.setChangeLogParameter("TABLE_NAME", "test_table")
        liquibase.updateTestingRollback(new Contexts(), new LabelExpression())

        then:
        def resultSet = h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def rsTableExist = h2.getConnection().createStatement().executeQuery("select count(1) from test_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
