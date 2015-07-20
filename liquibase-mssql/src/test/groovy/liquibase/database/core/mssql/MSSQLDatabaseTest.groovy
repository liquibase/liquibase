package liquibase.database.core.mssql

import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends Specification {

    def "does not support initially deferrable columns"() {
        expect:
        assert !new MSSQLDatabase().supportsInitiallyDeferrableColumns();
    }

    def "default driver"() {
        expect:
        new MSSQLDatabase().getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase") == "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        assert new MSSQLDatabase().getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase") == null
    }

    def "escapeTableName no schema"() {
        expect:
        new MSSQLDatabase().escapeTableName(null, null, "tableName") == "[tableName]"
    }

    def "escapeTableName with schema"() {
        expect:
        new MSSQLDatabase().escapeTableName("catalogName", "schemaName", "tableName") == "[schemaName].[tableName]"
    }

//    @Unroll
//    def "case sensitive depending on collation"() throws Exception {
//        when:
//        Database database = new MSSQLDatabase().withCollation(collation);
//
//        then:
//        database.isCaseSensitive(Table) == expected
//
//        where:
//        collation              | expected
//        "Latin1_General_BIN"   | true
//        "Latin1_General_CI_AI" | false
//        "Latin1_General_CS_AI" | true
//    }
}
