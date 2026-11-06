package liquibase.command

import liquibase.Scope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class PostgresQuestionMarkOperatorIntegrationTest extends Specification {

    private static final String CHANGELOG = "changelogs/pgsql/update/jsonb-question-mark-operators.xml"

    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope()
            .getSingleton(TestSystemFactory.class)
            .getTestSystem("postgresql")

    def "jsonb ?, ?| and ?& operators work in sql changes and update-sql output (#2032)"() {
        given:
        def customSchema = "jsonb_qm_" + System.currentTimeMillis()
        def connection = postgres.getDatabaseFromFactory().getConnection()
        connection.createStatement().execute("CREATE SCHEMA IF NOT EXISTS " + customSchema)
        connection.commit()
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        def updateSqlOutput = new ByteArrayOutputStream()

        when:
        Scope.child(scopeSettings, {
            CommandScope updateSql = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
            updateSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            updateSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            updateSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            updateSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, customSchema)
            updateSql.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, CHANGELOG)
            updateSql.setOutput(updateSqlOutput)
            updateSql.execute()

            CommandScope update = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            update.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            update.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            update.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            update.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, customSchema)
            update.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, CHANGELOG)
            update.execute()
        } as Scope.ScopedRunner)

        then:
        noExceptionThrown()

        // update-sql prints the operators as psql expects them, including the '??' escape
        def sql = updateSqlOutput.toString()
        sql.contains("WHERE j ? 'a'")
        sql.contains("WHERE j ?| array['c', 'x']")
        sql.contains("WHERE j ?& array['a', 'b']")
        sql.contains("WHERE j ? 'c'")
        sql.contains("'why?? and ?'")
        !sql.contains('$1')

        // update applied every operator against the actual jsonb rows
        def rs = connection.createStatement().executeQuery(
                "SELECT id, matched, note FROM ${customSchema}.jsonb_operators ORDER BY id")
        rs.next()
        rs.getString("matched") == "exists all "
        rs.getString("note") == "why?? and ?"
        rs.next()
        rs.getString("matched") == "any escaped "

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS " + customSchema + " CASCADE")
            connection.commit()
        } catch (Exception e) {}
    }
}
