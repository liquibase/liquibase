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
import spock.lang.Ignore
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
