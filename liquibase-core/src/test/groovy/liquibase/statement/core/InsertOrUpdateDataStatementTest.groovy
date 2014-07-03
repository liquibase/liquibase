package liquibase.statement.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InsertOrUpdateDataStatementTest extends InsertDataStatementTest {

    def constructor(){
        when:
        def statement = new InsertOrUpdateDataStatement("CATALOG", "SCHEMA","TABLE", "PRIMARYKEY");

        then:
        statement.getCatalogName() == "CATALOG"
        statement.getSchemaName() == "SCHEMA"
        statement.getTableName() == "TABLE"
        statement.getPrimaryKey() == "PRIMARYKEY"
    }

}
