package liquibase.diff.output.changelog.core

import liquibase.database.core.MockDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.ChangeGeneratorChain
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.Data
import liquibase.structure.core.Table
import spock.lang.Specification

import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement

class MissingDataChangeGeneratorTest extends Specification {

    def "fixMissing escapes column names containing double quotes to prevent SQL injection"() {
        given:
        def maliciousColumnName = 'secret" FROM evil_table --'
        def capturedSql = []

        def metaData = Mock(ResultSetMetaData) {
            getColumnCount() >> 1
            getColumnName(1) >> maliciousColumnName
        }
        def metaRs = Mock(ResultSet) {
            getMetaData() >> metaData
            next() >> false
        }
        def dataRs = Mock(ResultSet) {
            next() >> false
        }
        def stmt = Mock(Statement) {
            executeQuery(_) >> { String sql ->
                capturedSql << sql
                sql.contains("WHERE 1=0") ? metaRs : dataRs
            }
            setFetchSize(_) >> {}
        }
        def jdbcConn = Mock(Connection) {
            createStatement(_, _) >> stmt
        }
        def database = new MockDatabase() {
            @Override
            liquibase.database.DatabaseConnection getConnection() {
                new JdbcConnection(jdbcConn)
            }

            @Override
            String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
                '"' + objectName.replace('"', '""') + '"'
            }
        }

        def table = new Table("cat", "schema", "test_table")
        def data = new Data().setTable(table)
        def control = new DiffOutputControl()
        control.setIncludeObjects("column:.*")

        when:
        new MissingDataChangeGenerator().fixMissing(data, control, database, database, new ChangeGeneratorChain(null))

        then:
        def dataSql = capturedSql.find { !it.contains("WHERE 1=0") }
        dataSql.contains('"secret"" FROM evil_table --"')
        !dataSql.contains(maliciousColumnName)
    }
}
