package liquibase.integrationtest

import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.core.ParsedNode
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.sql.SqlConfiguration
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class RuntimePropertySetTest extends Specification {
    @Shared
    private DatabaseTestSystem h2 = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    private Database db = h2.databaseFromFactory

    def "set property from sql"() {
        final String prop = 'run-prop-id'

        when:
            Liquibase lb = new Liquibase('changelogs/runtime-props/main.xml', new SearchPathResourceAccessor(".,target/test-classes"), db)
            DatabaseChangeLog dbChangeLog = lb.getDatabaseChangeLog()
            lb.update()

        then:
            "1" == dbChangeLog.changeLogParameters.getValue(prop, dbChangeLog)
            "Rob" == dbChangeLog.changeLogParameters.getValue('runPropName', dbChangeLog)
    }
}
