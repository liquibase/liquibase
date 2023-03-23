package liquibase.command

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.command.core.UpdateCommandStep
import liquibase.database.core.H2Database
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

import java.sql.DriverManager
import java.sql.SQLException

import static java.lang.String.format

class UpdateCommandStepTest extends Specification {

    def "validate context and label entry has not been added previously"() {
        when:
        JdbcConnection h2Connection = getInMemoryH2DatabaseConnection();
        H2Database h2DB = new H2Database();
        h2DB.setConnection(h2Connection);
        Contexts context = new Contexts("testContext")
        LabelExpression label = new LabelExpression("testLabel")
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check-to-not-deploy.xml", new ClassLoaderResourceAccessor(),
                h2Connection)

        then:
        new UpdateCommandStep().isUpToDateFastCheck(null, h2DB, liquibase.getDatabaseChangeLog(), context, label) == false

        cleanup:
        h2Connection.close()
    }

    def "validate context and label entry has been added previously"() {
        when:

        JdbcConnection h2Connection = getInMemoryH2DatabaseConnection()
        H2Database h2DB = new H2Database()
        h2DB.setConnection(h2Connection)
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2Connection)
        Contexts context = new Contexts("testContext2")
        LabelExpression label = new LabelExpression("testLabel2")
        liquibase.update()

        then:
        new UpdateCommandStep().isUpToDateFastCheck(null, h2DB, liquibase.getDatabaseChangeLog(), context, label) == true

        cleanup:
        h2Connection.close()

    }

    private JdbcConnection getInMemoryH2DatabaseConnection() throws SQLException {
        String urlFormat = "jdbc:h2:mem:%s"
        return new JdbcConnection(DriverManager.getConnection(format(urlFormat, UUID.randomUUID().toString())))
    }
}