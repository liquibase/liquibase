package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;
import org.junit.Test;

public class AddAutoIncrementStatementTest extends AbstractStatementTest {
    
    def constructor() {
        when:
        def obj = new AddAutoIncrementStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", new BigInteger(10), new BigInteger(20))

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnName() == "COLUMN_NAME"
        obj.getColumnDataType() == "DATA_TYPE"
        obj.getStartWith() == new BigInteger(10)
        obj.getIncrementBy() == new BigInteger(20)
    }
}
