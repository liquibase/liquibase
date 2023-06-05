package liquibase.command

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateCommandStepIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "validate context and label entry has not been added previously"() {
        when:
        Contexts context = new Contexts("testContext")
        LabelExpression label = new LabelExpression("testLabel")
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check-to-not-deploy.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())

        then:
        !new UpdateCommandStep().isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "validate context and label entry has been added previously"() {
        when:
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())
        Contexts context = new Contexts("testContext2")
        LabelExpression label = new LabelExpression("testLabel2")
        liquibase.update()

        then:
        new UpdateCommandStep().isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}