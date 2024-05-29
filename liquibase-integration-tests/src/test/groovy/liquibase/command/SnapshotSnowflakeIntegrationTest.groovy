package liquibase.command


import liquibase.Scope
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

import static liquibase.command.util.CommandUtil.takeDatabaseSnapshot

@LiquibaseIntegrationTest
class SnapshotSnowflakeIntegrationTest extends Specification{

    @Shared
    private DatabaseTestSystem snowflake = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("snowflake") as DatabaseTestSystem

    def "snapshot table in another schema"() {
        def schemaName = StringUtil.randomIdentifier(10).toUpperCase()
        when:
        snowflake.executeSql("CREATE SCHEMA ${schemaName}")
        snowflake.executeSql("""
CREATE TABLE ${schemaName}.TABLE4 (
    col1 INTEGER NOT NULL,
    col2 INTEGER NOT NULL
    );   
""")
        snowflake.executeSql("ALTER TABLE ${schemaName}.table4 ADD CONSTRAINT UNIQUENESS UNIQUE(col1, col2) ENFORCED;")
        // Close the connection to force a new connection to be created using the default schema name (not the schema name from above)
        snowflake.getConnection().close()

        then:
        def snapshot = takeDatabaseSnapshot(snowflake.getDatabaseFromFactory(), schemaName)
        snapshot != null
        def tables = snapshot.get(Table.class)
        tables.size() == 1
        tables[0].getName() == "TABLE4"
        tables[0].getSchema().getName() == schemaName
        def uniqueConstraints = snapshot.get(UniqueConstraint)
        uniqueConstraints.size() == 1
        uniqueConstraints[0].getTable().getName() == "TABLE4"
        uniqueConstraints[0].getName() == "UNIQUENESS"

        cleanup:
        snowflake.executeSql("DROP SCHEMA ${schemaName}")
    }

}
