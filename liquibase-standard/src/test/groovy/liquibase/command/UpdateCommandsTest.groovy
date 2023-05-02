package liquibase.command

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.database.core.H2Database
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import spock.lang.Specification

import java.sql.DriverManager

class UpdateCommandsTest extends Specification {

    def jdbcURL = "jdbc:h2:mem:liquibase"
    def connection
    def jdbcConnection
    def database

    def setup() {
        connection =  DriverManager.getConnection(jdbcURL)
        connection.setAutoCommit(false)
        jdbcConnection = new JdbcConnection(connection)

        connection.createStatement().execute("DROP ALL OBJECTS")

        database = new H2Database()
        database.setConnection(new JdbcConnection(connection))
    }

    def "run UpdateSql from CommandStep"() {
        when:
        def updateSqlCommand = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
        updateSqlCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
        updateSqlCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")

        then:
        def result = updateSqlCommand.execute().getResults()
        def a = ((DefaultChangeExecListener)result.get("defaultChangeExecListener"))
        a.getDeployedChangeSets().get(0).getId() == "1"

        when:
        connection.createStatement().executeQuery("select count(1) from databasechangelog")

        then:
        final JdbcSQLSyntaxErrorException exception = thrown()
        exception.message.contains("this database is empty")
    }

    def "run Update from CommandStep"() {
        when:
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")

        and:
        updateCommand.execute()

        then:
        def resultSet = connection.createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def rsTableExist = connection.createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0
    }

    def "run Update from Liquibase class"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), database)
        liquibase.update(new Contexts())

        then:
        def resultSet = connection.createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def rsTableExist = connection.createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0
    }

    def "run Update from Liquibase class using print writer"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), database)
        liquibase.update(new Contexts(), new PrintWriter(System.out))
        connection.createStatement().executeQuery("select count(1) from databasechangelog")

        then:
        final JdbcSQLSyntaxErrorException exception = thrown()
        exception.message.contains("this database is empty")
    }
}
