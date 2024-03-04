package liquibase.command

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.database.core.InformixDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.executor.jvm.ColumnMapRowMapper
import liquibase.executor.jvm.RowMapperResultSetExtractor
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet
import java.sql.SQLException

@LiquibaseIntegrationTest
class UpdateSnowflakeIntegrationTest extends Specification{

    @Shared
    private DatabaseTestSystem snowflake = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("snowflake") as DatabaseTestSystem

    def "happy path update"() {
        when:
        CommandUtil.runUpdate(snowflake,'src/test/resources/changelogs/common/example-changelog.xml')

        then:
        noExceptionThrown()
    }

    def "another"() {
        when:
        snowflake.executeSql("""
CREATE TABLE table2 (
    col1 INTEGER NOT NULL,
    col2 INTEGER NOT NULL,
    CONSTRAINT pkey_1 PRIMARY KEY (col1, col2)
    );""")
        snowflake.executeSql("""
CREATE TABLE table3 (
    col_a INTEGER NOT NULL,
    col_b INTEGER NOT NULL,
    CONSTRAINT fkey_1 FOREIGN KEY (col_a, col_b) REFERENCES table2 (col1, col2)
    );
""")

        then:
        def keys = (snowflake.getDatabaseFromFactory().getConnection() as JdbcConnection).getUnderlyingConnection().getMetaData().getImportedKeys(
                snowflake.getDatabaseFromFactory().getDefaultCatalogName(),
                snowflake.getDatabaseFromFactory().getDefaultSchemaName(),
                "table3"
        )
        keys != null

        List<Map> result = (List<Map>) new RowMapperResultSetExtractor(new ColumnMapRowMapper(true)).extractData(keys)

        result != null

        cleanup:
        snowflake.executeSql("drop table table3")
        snowflake.executeSql("drop table table2")
    }
}
